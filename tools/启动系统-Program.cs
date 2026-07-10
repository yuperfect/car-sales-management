using System;
using System.Diagnostics;
using System.IO;
using System.Net.Http;
using System.Threading;

namespace CarSalesLauncher
{
    class Program
    {
        static readonly HttpClient client = new HttpClient { Timeout = TimeSpan.FromSeconds(3) };

        static void Main(string[] args)
        {
            Console.WriteLine("========================================");
            Console.WriteLine("  汽车销售管理系统 - 一键启动");
            Console.WriteLine("========================================");
            Console.WriteLine();

            string root = AppDomain.CurrentDomain.BaseDirectory;

            if (!Directory.Exists(Path.Combine(root, "code", "car-sales-backend")))
            {
                Console.WriteLine("[FAIL] 项目目录结构不正确(code/car-sales-backend不存在)");
                Console.WriteLine("       请将本工具放在项目根目录下运行");
                Console.WriteLine("按任意键退出...");
                Console.ReadKey();
                return;
            }

            string javaPath = DetectJava();
            if (javaPath == null)
            {
                Console.WriteLine("[FAIL] 未检测到 JDK，请确保 JDK 17+ 已安装并添加到 PATH");
                Console.WriteLine("按任意键退出...");
                Console.ReadKey();
                return;
            }

            // ========== 前置检查：JAR 文件 ==========
            string jarPath = Path.Combine(root, "code", "car-sales-backend", "target", "car-sales-backend-1.0.0.jar");
            if (!File.Exists(jarPath))
            {
                Console.WriteLine("[FAIL] 后端 JAR 文件不存在，请先运行 配置检查.exe 编译后端");
                Console.WriteLine("按任意键退出...");
                Console.ReadKey();
                return;
            }

            bool allSuccess = true;

            // ========== 1. 启动后端 ==========
            Console.WriteLine("[1/3] 启动后端 (Spring Boot :8080)...");
            Process.Start(new ProcessStartInfo
            {
                FileName = "cmd.exe",
                Arguments = $"/c \"\"{javaPath}\" -jar \"{jarPath}\" --server.port=8080\"",
                WorkingDirectory = root,
                WindowStyle = ProcessWindowStyle.Normal,
                UseShellExecute = true
            });
            if (!WaitForPort("后端", "http://localhost:8080/api/cars", 60)) allSuccess = false;

            // ========== 2. 启动管理端 ==========
            Console.WriteLine("\n[2/3] 启动管理端 (Vite :5173)...");
            Process.Start(new ProcessStartInfo
            {
                FileName = "cmd.exe",
                Arguments = $"/k \"cd /d \"{Path.Combine(root, "code", "car-sales-admin")}\" && npm run dev\"",
                WorkingDirectory = root,
                WindowStyle = ProcessWindowStyle.Normal,
                UseShellExecute = true
            });
            if (!WaitForPort("管理端", "http://localhost:5173", 40)) allSuccess = false;

            // ========== 3. 启动客户端 ==========
            Console.WriteLine("\n[3/3] 启动客户端 (Vite :3000)...");
            Process.Start(new ProcessStartInfo
            {
                FileName = "cmd.exe",
                Arguments = $"/k \"cd /d \"{Path.Combine(root, "code", "car-sales-client")}\" && npm run dev\"",
                WorkingDirectory = root,
                WindowStyle = ProcessWindowStyle.Normal,
                UseShellExecute = true
            });
            if (!WaitForPort("客户端", "http://localhost:3000", 40)) allSuccess = false;

            // ========== 打开浏览器 ==========
            Console.WriteLine("\n正在打开浏览器...");
            Thread.Sleep(2000);

            try
            {
                Process.Start(new ProcessStartInfo
                {
                    FileName = "cmd.exe",
                    Arguments = "/c start http://localhost:3000",
                    UseShellExecute = false,
                    CreateNoWindow = true
                });
                Console.WriteLine("  [OK] 客户端 http://localhost:3000");
            }
            catch (Exception ex) { Console.WriteLine($"  [FAIL] 打开客户端浏览器失败: {ex.Message}"); }

            Thread.Sleep(1000);

            try
            {
                Process.Start(new ProcessStartInfo
                {
                    FileName = "cmd.exe",
                    Arguments = "/c start http://localhost:5173/admin",
                    UseShellExecute = false,
                    CreateNoWindow = true
                });
                Console.WriteLine("  [OK] 管理端 http://localhost:5173/admin");
            }
            catch (Exception ex) { Console.WriteLine($"  [FAIL] 打开管理端浏览器失败: {ex.Message}"); }

            Console.WriteLine();
            Console.WriteLine("========================================");
            if (allSuccess)
                Console.WriteLine("  三个服务已全部启动!");
            else
                Console.WriteLine("  部分服务启动失败，请检查上方日志");
            Console.WriteLine();
            Console.WriteLine("  后端:    http://localhost:8080");
            Console.WriteLine("  管理端:  http://localhost:5173/admin");
            Console.WriteLine("  客户端:  http://localhost:3000");
            Console.WriteLine();
            Console.WriteLine("  已自动打开浏览器访问客户端和管理端。");
            Console.WriteLine("  关闭方式: 双击 关闭系统.exe");
            Console.WriteLine("========================================");
            Console.WriteLine();
            Console.WriteLine("按任意键退出（服务将在后台继续运行）...");
            Console.ReadKey();
        }

        static string DetectJava()
        {
            // 1. 优先检查 JAVA_HOME 环境变量
            try
            {
                string javaHome = Environment.GetEnvironmentVariable("JAVA_HOME");
                if (!string.IsNullOrEmpty(javaHome))
                {
                    string javaExe = Path.Combine(javaHome, "bin", "java.exe");
                    if (File.Exists(javaExe))
                        return javaExe;
                }
            }
            catch { }

            // 2. 从 PATH 查找 java.exe
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
                    if (File.Exists(javaExe))
                        return javaExe;
                }
            }
            catch { }

            // 3. 兜底：直接用 PATH 上的 java 命令
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

        static bool WaitForPort(string serviceName, string url, int maxSeconds)
        {
            for (int i = maxSeconds; i > 0; i--)
            {
                Console.Write($"  \r[..] 等待 {serviceName} 就绪... {i,2}秒   ");

                try
                {
                    var response = client.GetAsync(url).GetAwaiter().GetResult();
                    if (response.IsSuccessStatusCode)
                    {
                        int elapsed = maxSeconds - i + 1;
                        Console.Write($"\r  [OK] {serviceName} 已启动! (共耗时 {elapsed}秒)          \n");
                        return true;
                    }
                }
                catch
                {
                }

                Thread.Sleep(1000);
            }

            Console.Write($"\r  [FAIL] {serviceName} 启动超时 ({maxSeconds}秒)，请检查环境配置          \n");
            return false;
        }
    }
}
