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

            string javaHome = DetectJava();
            if (javaHome == null)
            {
                Console.WriteLine("[FAIL] 未检测到 JDK，请确保 JDK 17+ 已安装并添加到 PATH");
                Console.WriteLine("按任意键退出...");
                Console.ReadKey();
                return;
            }

            // ========== 1. 启动后端 ==========
            Console.WriteLine("[1/3] 启动后端 (Spring Boot :8080)...");
            Process.Start(new ProcessStartInfo
            {
                FileName = "cmd.exe",
                Arguments = $"/c \"\"{Path.Combine(javaHome, "bin", "java")}\" -jar \"{Path.Combine(root, "code", "car-sales-backend", "target", "car-sales-backend-1.0.0.jar")}\" --server.port=8080\"",
                WorkingDirectory = root,
                WindowStyle = ProcessWindowStyle.Normal,
                UseShellExecute = true
            });
            WaitForPort("后端", "http://localhost:8080/api/cars/1", 60);

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
            WaitForPort("管理端", "http://localhost:5173", 40);

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
            WaitForPort("客户端", "http://localhost:3000", 40);

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
            Console.WriteLine("  三个服务已全部启动!");
            Console.WriteLine();
            Console.WriteLine("  后端:    http://localhost:8080");
            Console.WriteLine("  管理端:  http://localhost:5173/admin");
            Console.WriteLine("  客户端:  http://localhost:3000");
            Console.WriteLine();
            Console.WriteLine("  已自动打开浏览器访问客户端和管理端。");
            Console.WriteLine("  关闭方式: 双击 关闭系统.exe 或 stop.bat");
            Console.WriteLine("========================================");
            Console.WriteLine();
            Console.WriteLine("按任意键退出（服务将在后台继续运行）...");
            Console.ReadKey();
        }

        static string DetectJava()
        {
            try
            {
                var psi = new ProcessStartInfo
                {
                    FileName = "cmd.exe",
                    Arguments = "/c java -version",
                    UseShellExecute = false,
                    RedirectStandardOutput = true,
                    RedirectStandardError = true,
                    CreateNoWindow = true
                };
                var p = Process.Start(psi);
                string output = p.StandardError.ReadToEnd();
                p.WaitForExit(5000);
                if (p.ExitCode == 0 && output.Contains("version", StringComparison.OrdinalIgnoreCase))
                    return "";
            }
            catch { }

            // 尝试 JAVA_HOME
            try
            {
                string javaHome = Environment.GetEnvironmentVariable("JAVA_HOME");
                if (!string.IsNullOrEmpty(javaHome) && File.Exists(Path.Combine(javaHome, "bin", "java.exe")))
                    return javaHome;
            }
            catch { }

            return null;
        }

        static void WaitForPort(string serviceName, string url, int maxSeconds)
        {
            for (int i = maxSeconds; i > 0; i--)
            {
                Console.Write($"  \r[..] 等待 {serviceName} 就绪... {i,2}秒   ");

                try
                {
                    var response = client.GetAsync(url).GetAwaiter().GetResult();
                    if (response.IsSuccessStatusCode || response.StatusCode == System.Net.HttpStatusCode.InternalServerError)
                    {
                        int elapsed = maxSeconds - i + 1;
                        Console.Write($"\r  [OK] {serviceName} 已启动! (共耗时 {elapsed}秒)          \n");
                        return;
                    }
                }
                catch
                {
                }

                Thread.Sleep(1000);
            }

            Console.Write($"\r  [FAIL] {serviceName} 启动超时 ({maxSeconds}秒)，请检查环境配置          \n");
        }
    }
}
