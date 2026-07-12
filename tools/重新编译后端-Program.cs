using System;
using System.Diagnostics;
using System.IO;
using System.Text;
using System.Threading;

namespace CarSalesRebuild
{
    class Program
    {
        static string root = AppDomain.CurrentDomain.BaseDirectory;

        static void Main(string[] args)
        {
            Console.OutputEncoding = Encoding.UTF8;

            Console.WriteLine("========================================");
            Console.WriteLine("  汽车销售管理系统 - 重新编译后端");
            Console.WriteLine("========================================");
            Console.WriteLine();

            string backendDir = Path.Combine(root, "code", "car-sales-backend");
            string mvnw = Path.Combine(backendDir, "mvnw.cmd");

            if (!Directory.Exists(backendDir))
            {
                Console.WriteLine("[FAIL] 后端目录不存在: " + backendDir);
                Console.WriteLine("按任意键退出...");
                Console.ReadKey();
                return;
            }

            if (!File.Exists(mvnw))
            {
                Console.WriteLine("[FAIL] 找不到 mvnw.cmd: " + mvnw);
                Console.WriteLine("按任意键退出...");
                Console.ReadKey();
                return;
            }

            // ===== 检测 Java =====
            Console.Write("[1/3] 检测 Java 环境...");
            string javaPath = DetectJava();
            if (javaPath == null)
            {
                Console.Write("\r[FAIL] 未检测到 JDK 17+，请确保已安装并添加到 PATH\n");
                Console.WriteLine("按任意键退出...");
                Console.ReadKey();
                return;
            }
            Console.Write($"\r[OK] Java 环境就绪 ({javaPath})          \n");

            // ===== 清理旧编译产物 =====
            Console.Write("[2/3] 清理旧的编译产物...");
            string targetDir = Path.Combine(backendDir, "target");
            if (Directory.Exists(targetDir))
            {
                try
                {
                    Directory.Delete(targetDir, true);
                    Console.Write("\r[OK] 已清理 target 目录                           \n");
                }
                catch (Exception ex)
                {
                    Console.Write($"\r[WARN] 清理失败: {ex.Message}                       \n");
                    Console.WriteLine("  将跳过清理，直接编译...");
                }
            }
            else
            {
                Console.Write("\r[OK] 无需清理（target 不存在）                    \n");
            }

            // ===== Maven 编译 =====
            Console.WriteLine("[3/3] Maven 编译中 (mvnw clean package -DskipTests)...");
            Console.WriteLine("  此过程可能需要 2-5 分钟，请耐心等待...");
            Console.WriteLine();

            string jarPath = Path.Combine(backendDir, "target", "car-sales-backend-1.0.0.jar");

            bool success = false;
            string buildOutput = "";

            try
            {
                var psi = new ProcessStartInfo
                {
                    FileName = "cmd.exe",
                    Arguments = $"/c mvnw.cmd -Dmaven.multiModuleProjectDirectory=\"{backendDir}\" clean package -DskipTests",
                    WorkingDirectory = backendDir,
                    UseShellExecute = false,
                    RedirectStandardOutput = true,
                    RedirectStandardError = true,
                    CreateNoWindow = true
                };

                using (var p = Process.Start(psi))
                {
                    var outTask = p.StandardOutput.ReadToEndAsync();
                    var errTask = p.StandardError.ReadToEndAsync();

                    // 显示进度动画
                    string[] spinner = { "|", "/", "-", "\\" };
                    int spinIdx = 0;
                    int seconds = 0;

                    while (!p.HasExited)
                    {
                        if (p.WaitForExit(1000)) break;
                        seconds++;
                        Console.Write($"\r  [{spinner[spinIdx]}] 编译中... 已等待 {seconds} 秒");
                        spinIdx = (spinIdx + 1) % 4;
                    }

                    buildOutput = outTask.GetAwaiter().GetResult() + errTask.GetAwaiter().GetResult();

                    Console.Write($"\r  编译完成，耗时 {seconds} 秒                           \n\n");

                    if (p.ExitCode == 0 && File.Exists(jarPath))
                    {
                        success = true;
                    }
                }
            }
            catch (Exception ex)
            {
                buildOutput = ex.ToString();
            }

            // ===== 结果 =====
            Console.WriteLine("========================================");
            if (success)
            {
                long size = new FileInfo(jarPath).Length;
                Console.WriteLine($"  [SUCCESS] 编译成功! ({size / 1024 / 1024} MB)");
                Console.WriteLine($"  JAR 路径: {jarPath}");
                Console.WriteLine();
                Console.WriteLine("  现在可以双击 启动系统.exe 启动服务了。");
            }
            else
            {
                Console.WriteLine("  [FAIL] 编译失败!");
                Console.WriteLine();

                // 显示最后几行输出
                if (!string.IsNullOrEmpty(buildOutput))
                {
                    string[] lines = buildOutput.Split('\n');
                    Console.WriteLine("  === 编译输出（最后 15 行）===");
                    int start = Math.Max(0, lines.Length - 15);
                    for (int i = start; i < lines.Length; i++)
                    {
                        string line = lines[i].Trim();
                        if (!string.IsNullOrEmpty(line))
                            Console.WriteLine($"    {line}");
                    }
                }

                Console.WriteLine();
                Console.WriteLine("  常见问题:");
                Console.WriteLine("    - JDK 版本不匹配，需要 JDK 17+");
                Console.WriteLine("    - Maven 依赖下载失败，请检查网络");
                Console.WriteLine("    - 代码有语法错误，请检查上方输出");
            }
            Console.WriteLine("========================================");
            Console.WriteLine();
            Console.WriteLine("按任意键退出...");
            Console.ReadKey();
        }

        static string DetectJava()
        {
            // 1. JAVA_HOME
            try
            {
                string javaHome = Environment.GetEnvironmentVariable("JAVA_HOME");
                if (!string.IsNullOrEmpty(javaHome))
                {
                    string javaExe = Path.Combine(javaHome, "bin", "java.exe");
                    if (File.Exists(javaExe)) return javaExe;
                }
            }
            catch { }

            // 2. where java
            try
            {
                var psi = new ProcessStartInfo("cmd.exe", "/c where java")
                {
                    UseShellExecute = false,
                    RedirectStandardOutput = true,
                    CreateNoWindow = true
                };
                var p = Process.Start(psi);
                string output = p.StandardOutput.ReadToEnd().Trim();
                p.WaitForExit(3000);
                if (p.ExitCode == 0 && !string.IsNullOrEmpty(output))
                {
                    string javaExe = output.Split('\n', '\r')[0].Trim();
                    if (File.Exists(javaExe)) return javaExe;
                }
            }
            catch { }

            // 3. java -version
            try
            {
                var psi = new ProcessStartInfo("cmd.exe", "/c java -version")
                {
                    UseShellExecute = false,
                    RedirectStandardError = true,
                    CreateNoWindow = true
                };
                var p = Process.Start(psi);
                string err = p.StandardError.ReadToEnd();
                p.WaitForExit(3000);
                if (p.ExitCode == 0 && err.Contains("version", StringComparison.OrdinalIgnoreCase))
                    return "java";
            }
            catch { }

            return null;
        }
    }
}
