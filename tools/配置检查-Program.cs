using System;
using System.Diagnostics;
using System.IO;
using System.Net.NetworkInformation;
using System.Text;

namespace CarSalesChecker
{
    class Program
    {
        static string root = AppDomain.CurrentDomain.BaseDirectory;
        static string mysqlUser = "root";
        static string mysqlPass = "";
        static int okCount = 0;
        static int failCount = 0;

        static void Main(string[] args)
        {
            Console.OutputEncoding = Encoding.UTF8;
            Console.InputEncoding = Encoding.UTF8;

            Console.WriteLine("========================================");
            Console.WriteLine("  汽车销售管理系统 - 环境配置检查");
            Console.WriteLine("========================================");
            Console.WriteLine();

            if (!Directory.Exists(root))
            {
                WriteFail("项目目录", $"不存在 - {root}");
                Console.WriteLine("按任意键退出...");
                Console.ReadKey();
                return;
            }

            // ===== 1. JDK =====
            StepTitle(1, "JDK 环境");
            CheckJDK();

            // ===== 2. Node.js + npm =====
            StepTitle(2, "Node.js + npm");
            CheckNode();

            // ===== 3. 后端Jar =====
            StepTitle(3, "后端编译产物");
            CheckBackendJar();

            // ===== 4. 前端依赖 =====
            StepTitle(4, "前端依赖 (node_modules)");
            CheckFrontendDeps();

            // ===== 5. 端口占用 =====
            StepTitle(5, "端口占用检查");
            CheckPorts();

            // ===== 6. MySQL连接 =====
            StepTitle(6, "数据库连接配置");
            CheckMySQL();

            // ===== 7. 数据库是否存在 =====
            StepTitle(7, "数据库 (car_sales_db)");
            CheckDatabase();

            // ===== 8. 更新配置文件 =====
            StepTitle(8, "更新配置文件");
            UpdateConfig();

            // ===== 9. 检查是否能正常打开网页 =====
            StepTitle(9, "前端构建状态");
            CheckFrontendBuild();

            // ===== 汇总 =====
            Console.WriteLine();
            Console.WriteLine("========================================");
            if (failCount == 0)
            {
                Console.WriteLine($"  环境检查通过! 共 {okCount + failCount}/{okCount + failCount} 项正常");
                Console.WriteLine("  你可以双击 启动系统.exe 启动了。");
            }
            else
            {
                Console.WriteLine($"  检查完成 - {failCount} 项异常，{okCount} 项正常");
                Console.WriteLine("  请修复上述问题后重新运行本工具。");
            }
            Console.WriteLine("========================================");
            Console.WriteLine();
            Console.WriteLine("按任意键退出...");
            try { Console.ReadKey(); } catch { Console.ReadLine(); }
        }

        // ================================================================
        //  检查项实现
        // ================================================================

        static void CheckJDK()
        {
            var (ok, output) = RunCmd("cmd.exe", "/c java -version");
            if (ok && output.Contains("version", StringComparison.OrdinalIgnoreCase))
            {
                string verLine = "";
                foreach (var line in output.Split('\n'))
                {
                    if (line.Contains("version")) { verLine = line.Trim(); break; }
                }
                WriteOk("JDK", verLine);
            }
            else
            {
                WriteFail("JDK", "未找到 (请确保 JDK 17+ 已安装并添加到 PATH)");
                WriteHint("安装 JDK 后需确保 JAVA_HOME 环境变量或 PATH 中包含 java 命令");
            }
        }

        static void CheckNode()
        {
            // Node.js (用cmd /c确保PATH生效)
            var (nodeOk, nodeOut) = RunCmd("cmd.exe", "/c node --version");
            if (nodeOk && !string.IsNullOrWhiteSpace(nodeOut))
            {
                string ver = nodeOut.Trim().Split('\n')[0].Trim();
                WriteOk("Node.js", ver);

                // npm (用cmd /c确保PATH生效)
                var (npmOk, npmOut) = RunCmd("cmd.exe", "/c npm --version");
                if (npmOk && !string.IsNullOrWhiteSpace(npmOut))
                {
                    string npmVer = npmOut.Trim().Split('\n')[0].Trim();
                    WriteOk("npm", npmVer);
                }
                else
                {
                    WriteFail("npm", "未找到 npm");
                    WriteHint("请确保 Node.js 安装时包含了 npm");
                }
            }
            else
            {
                WriteFail("Node.js", "未找到 (请确保 Node.js 已安装并添加到 PATH)");
                WriteHint("前往 https://nodejs.org 下载安装 LTS 版本");
            }
        }

        static void CheckBackendJar()
        {
            string jarPath = Path.Combine(root, "code", "car-sales-backend", "target", "car-sales-backend-1.0.0.jar");
            if (File.Exists(jarPath))
            {
                long size = new FileInfo(jarPath).Length;
                WriteOk("后端Jar", $"已编译 ({size / 1024 / 1024}MB)");
                return;
            }

            WriteFail("后端Jar", "未编译，需要执行 mvnw.cmd package");
            if (AskUser("是否自动执行 Maven 编译?"))
            {
                Console.Write("  正在编译 (mvnw.cmd clean package -DskipTests)...");
                string mvnw = Path.Combine(root, "code", "car-sales-backend", "mvnw.cmd");
                if (File.Exists(mvnw))
                {
                    string backendDir = Path.Combine(root, "code", "car-sales-backend");
                    var (ok, output) = RunMaven(backendDir, 600);
                    if (ok && File.Exists(jarPath))
                    {
                        Console.Write("\r  [OK] 后端编译成功!                            \n");
                        okCount++;
                        return;
                    }
                    else
                    {
                        Console.Write("\r  [FAIL] 编译失败                               \n");
                        string[] lines = output.Split('\n');
                        int start = Math.Max(0, lines.Length - 8);
                        for (int i = start; i < lines.Length; i++)
                        {
                            if (!string.IsNullOrWhiteSpace(lines[i]))
                                Console.WriteLine($"    {lines[i].Trim()}");
                        }
                        failCount++;
                        return;
                    }
                }
                else
                {
                    Console.Write("\r  [FAIL] 找不到 mvnw.cmd                           \n");
                    failCount++;
                    return;
                }
            }
            failCount++;
        }

        static void CheckFrontendDeps()
        {
            string[] dirs = {
                Path.Combine(root, "code", "car-sales-admin"),
                Path.Combine(root, "code", "car-sales-client")
            };
            bool allOk = true;

            foreach (string dir in dirs)
            {
                string nm = Path.Combine(dir, "node_modules");
                string name = new DirectoryInfo(dir).Name;
                if (Directory.Exists(nm))
                {
                    WriteOk($"{name} node_modules", "已安装");
                }
                else
                {
                    WriteFail($"{name} node_modules", "未安装");
                    allOk = false;
                    if (AskUser($"是否自动执行 npm install ( {name} )?"))
                    {
                        Console.Write($"  正在安装 {name} 依赖...");
                        var (ok, _) = RunCmdWithTimeout("cmd.exe", $"/c cd /d \"{dir}\" && npm install", root, 180);
                        if (ok && Directory.Exists(nm))
                        {
                            Console.Write($"\r  [OK] {name} 依赖安装完成                    \n");
                        }
                        else
                        {
                            Console.Write($"\r  [FAIL] 安装失败，请手动执行 npm install     \n");
                            failCount++;
                            return;
                        }
                    }
                    else
                    {
                        failCount++;
                        return;
                    }
                }
            }

            if (allOk) okCount++;
        }

        static void CheckPorts()
        {
            int[] ports = { 8080, 5173, 3000 };
            bool allFree = true;

            foreach (int port in ports)
            {
                try
                {
                    var connections = System.Net.NetworkInformation.IPGlobalProperties.GetIPGlobalProperties()
                        .GetActiveTcpListeners();
                    bool inUse = false;
                    foreach (var ep in connections)
                    {
                        if (ep.Port == port) { inUse = true; break; }
                    }

                    if (inUse)
                    {
                        WriteFail($"端口 {port}", "已被占用");
                        WriteHint("请先关闭占用该端口的程序，或使用 关闭系统.exe");
                        allFree = false;
                    }
                    else
                    {
                        WriteOk($"端口 {port}", "空闲可用");
                    }
                }
                catch
                {
                    WriteOk($"端口 {port}", "无法检测 (跳过)");
                }
            }

            if (allFree) okCount++; else failCount++;
        }

        static void CheckMySQL()
        {
            Console.WriteLine("  请输入 MySQL 连接信息:");
            Console.Write("  用户名 (默认: root): ");
            string input = Console.ReadLine();
            if (!string.IsNullOrWhiteSpace(input)) mysqlUser = input;

            Console.Write("  密  码: ");
            mysqlPass = ReadPassword();

            Console.Write("  正在连接数据库...");
            bool connected = TestMySQLConnection();
            if (connected)
            {
                Console.Write("\r  [OK] 数据库连接成功!                     \n");
                okCount++;
            }
            else
            {
                Console.Write("\r  [FAIL] 连接失败                            \n");
                Console.WriteLine("  可能的原因:");
                Console.WriteLine("    - MySQL 服务未启动, 请启动 MySQL");
                Console.WriteLine("    - 用户名或密码错误, 请重新输入");
                Console.WriteLine("    - 端口不是 3306, 请检查配置");
                Console.WriteLine();
                if (AskUser("是否重新输入?"))
                {
                    CheckMySQL();
                    return;
                }
                failCount++;
            }
        }

        static void CheckDatabase()
        {
            string sql = "CREATE DATABASE IF NOT EXISTS car_sales_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;";
            var (ok, _) = ExecuteSQL(sql, 30);
            if (ok)
            {
                WriteOk("数据库", "car_sales_db 已存在/已创建");
                okCount++;
            }
            else
            {
                WriteFail("数据库", "创建/检查失败");
                if (AskUser("是否重试?"))
                {
                    CheckDatabase();
                    return;
                }
                failCount++;
            }
        }

        static void UpdateConfig()
        {
            string configPath = Path.Combine(root, "code", "car-sales-backend", "src", "main", "resources", "application.yml");
            if (!File.Exists(configPath))
            {
                WriteFail("配置文件", "找不到 application.properties");
                failCount++;
                return;
            }

string content = File.ReadAllText(configPath, Encoding.UTF8);
            bool changed = false;
            string[] lines = content.Split('\n');

            for (int i = 0; i < lines.Length; i++)
            {
                string trimmed = lines[i].TrimStart();
                // YAML: "    username: root" -> "    username: newUser"
                if (trimmed.StartsWith("username:") && trimmed.Contains(": "))
                {
                    string indent = lines[i].Substring(0, lines[i].Length - lines[i].TrimStart().Length);
                    lines[i] = indent + "username: " + mysqlUser;
                    changed = true;
                }
                // YAML: "    password: 123456" -> "    password: newPass"
                if (trimmed.StartsWith("password:") && trimmed.Contains(": "))
                {
                    string indent = lines[i].Substring(0, lines[i].Length - lines[i].TrimStart().Length);
                    lines[i] = indent + "password: " + mysqlPass;
                    changed = true;
                }
            }

            if (changed)
                content = string.Join("\n", lines);

            if (changed)
            {
                File.WriteAllText(configPath, content, Encoding.UTF8);
                WriteOk("配置文件", "已更新数据库连接信息");
            }
            else
            {
                WriteOk("配置文件", "无需修改");
            }
            okCount++;
        }

        static void CheckFrontendBuild()
        {
            // 检查前端vite.config是否存在，确认可以正常启动
            string adminVite = Path.Combine(root, "code", "car-sales-admin", "vite.config.js");
            string clientVite = Path.Combine(root, "code", "car-sales-client", "vite.config.js");
            if (File.Exists(adminVite) && File.Exists(clientVite))
            {
                WriteOk("前端构建", "vite 配置正常，可以启动");
                okCount++;
            }
            else
            {
                WriteFail("前端构建", "vite.config.ts 缺失");
                failCount++;
            }
        }

        // ================================================================
        //  辅助函数
        // ================================================================

        static void StepTitle(int num, string title)
        {
            Console.WriteLine($"--- [{num}/9] {title} ---");
        }

        static void WriteOk(string item, string msg)
        {
            Console.WriteLine($"  [OK] {item}: {msg}");
        }

        static void WriteFail(string item, string msg)
        {
            Console.WriteLine($"  [FAIL] {item}: {msg}");
        }

        static void WriteHint(string msg)
        {
            Console.WriteLine($"        提示: {msg}");
        }

        static bool AskUser(string question)
        {
            Console.Write($"  [..] {question} (Y/n): ");
            try
            {
                var key = Console.ReadKey();
                Console.WriteLine();
                return key.Key == ConsoleKey.Y || key.Key == ConsoleKey.Enter;
            }
            catch (InvalidOperationException)
            {
                // 输入被重定向，用 ReadLine 代替
                string input = Console.ReadLine();
                return input?.Trim().ToLower() == "y" || string.IsNullOrWhiteSpace(input);
            }
        }

        static (bool success, string output) RunCmd(string exe, string args)
        {
            try
            {
                var psi = new ProcessStartInfo
                {
                    FileName = exe,
                    Arguments = args,
                    UseShellExecute = false,
                    RedirectStandardOutput = true,
                    RedirectStandardError = true,
                    CreateNoWindow = true
                };
                var p = Process.Start(psi);
                string output = p.StandardOutput.ReadToEnd();
                string error = p.StandardError.ReadToEnd();
                p.WaitForExit(10000);
                return (p.ExitCode == 0, output + error);
            }
            catch { return (false, ""); }
        }

        static (bool ok, string output) RunCmdWithTimeout(string exe, string args, string workDir, int timeoutSec)
        {
            try
            {
                var psi = new ProcessStartInfo
                {
                    FileName = exe,
                    Arguments = args,
                    WorkingDirectory = workDir,
                    UseShellExecute = false,
                    RedirectStandardOutput = true,
                    RedirectStandardError = true,
                    CreateNoWindow = true
                };
                var p = Process.Start(psi);
                var outTask = p.StandardOutput.ReadToEndAsync();
                var errTask = p.StandardError.ReadToEndAsync();
                bool exited = p.WaitForExit(timeoutSec * 1000);
                if (exited)
                {
                    string output = outTask.GetAwaiter().GetResult();
                    output += errTask.GetAwaiter().GetResult();
                    return (p.ExitCode == 0, output);
                }
                try { p.Kill(); } catch { }
                return (false, "");
            }
            catch { return (false, ""); }
        }

        static (bool ok, string output) RunMaven(string workDir, int timeoutSec)
        {
            try
            {
                var psi = new ProcessStartInfo
                {
                    FileName = "cmd.exe",
                    Arguments = $"/c mvnw.cmd -Dmaven.multiModuleProjectDirectory=\"{workDir}\" clean package -DskipTests",
                    WorkingDirectory = workDir,
                    UseShellExecute = false,
                    RedirectStandardOutput = true,
                    RedirectStandardError = true,
                    CreateNoWindow = true
                };
                var p = Process.Start(psi);
                var outTask = p.StandardOutput.ReadToEndAsync();
                var errTask = p.StandardError.ReadToEndAsync();
                bool exited = p.WaitForExit(timeoutSec * 1000);
                if (exited)
                {
                    string output = outTask.GetAwaiter().GetResult();
                    output += errTask.GetAwaiter().GetResult();
                    return (p.ExitCode == 0, output);
                }
                try { p.Kill(); } catch { }
                return (false, "");
            }
            catch { return (false, ""); }
        }

        static bool TestMySQLConnection()
        {
            var (ok, output) = RunCmd("cmd.exe",
                $"/c mysql -u{mysqlUser} -p{mysqlPass} -e \"SELECT 1\" --connect-timeout=5");
            return ok;
        }

        static (bool ok, string error) ExecuteSQL(string sql, int timeoutSec)
        {
            try
            {
                // Write SQL to temp file and execute with mysql CLI
                string tempFile = Path.Combine(Path.GetTempPath(), "carsales_exec_" + Guid.NewGuid().ToString("N") + ".sql");
                File.WriteAllText(tempFile, sql, Encoding.UTF8);

                var (ok, err) = RunCmdWithTimeout("cmd.exe",
                    $"/c mysql -u{mysqlUser} -p{mysqlPass} < \"{tempFile}\"",
                    root, timeoutSec);

                try { File.Delete(tempFile); } catch { }
                return (ok, err);
            }
            catch (Exception ex) { return (false, ex.Message); }
        }

        static string ReadPassword()
        {
            try
            {
                string pass = "";
                ConsoleKeyInfo key;
                do
                {
                    key = Console.ReadKey(true);
                    if (key.Key != ConsoleKey.Enter && key.Key != ConsoleKey.Backspace)
                    {
                        pass += key.KeyChar;
                        Console.Write("*");
                    }
                    else if (key.Key == ConsoleKey.Backspace && pass.Length > 0)
                    {
                        pass = pass.Substring(0, pass.Length - 1);
                        Console.Write("\b \b");
                    }
                } while (key.Key != ConsoleKey.Enter);
                Console.WriteLine();
                return pass;
            }
            catch (InvalidOperationException)
            {
                // 输入被重定向（管道模式），直接用ReadLine
                return Console.ReadLine();
            }
        }
    }
}