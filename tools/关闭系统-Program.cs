using System;
using System.Diagnostics;
using System.IO;

namespace CarSalesStop
{
    class Program
    {
        static void Main(string[] args)
        {
            Console.OutputEncoding = System.Text.Encoding.UTF8;
            Console.WriteLine("========================================");
            Console.WriteLine("  汽车销售管理系统 - 一键关闭");
            Console.WriteLine("========================================");
            Console.WriteLine();
            Console.WriteLine("正在关闭所有服务...");

            int[] ports = { 8080, 5173, 3000 };
            int stopped = 0;

            // --- 方式一：通过窗口标题杀进程 ---
            string[] windowTitles = { "CarSales-Backend", "CarSales-Admin", "CarSales-Client" };
            foreach (string title in windowTitles)
            {
                try
                {
                    var psi = new ProcessStartInfo("taskkill", $"/f /fi \"WINDOWTITLE eq {title}\"")
                    {
                        UseShellExecute = false,
                        CreateNoWindow = true,
                        RedirectStandardOutput = true,
                        RedirectStandardError = true
                    };
                    var p = Process.Start(psi);
                    string output = p.StandardOutput.ReadToEnd() + p.StandardError.ReadToEnd();
                    p.WaitForExit(2000);
                    if (output.Contains("成功"))
                    {
                        Console.WriteLine($"  [OK] 已关闭 {title}");
                        stopped++;
                    }
                }
                catch { }
            }

            // --- 方式二：通过端口杀进程 ---
            foreach (int port in ports)
            {
                try
                {
                    var psi = new ProcessStartInfo("powershell", $"-NoProfile -Command \"$c=Get-NetTCPConnection -LocalPort {port} -ErrorAction SilentlyContinue | Select-Object -First 1; if($c -and $c.OwningProcess -gt 0){{Stop-Process -Id $c.OwningProcess -Force -ErrorAction SilentlyContinue; Write-Host 'closed'}}\"")
                    {
                        UseShellExecute = false,
                        CreateNoWindow = true,
                        RedirectStandardOutput = true
                    };
                    var proc = Process.Start(psi);
                    string output = proc.StandardOutput.ReadToEnd();
                    proc.WaitForExit(3000);
                    if (output.Contains("closed"))
                    {
                        string name = port switch { 8080 => "后端", 5173 => "管理端", 3000 => "客户端", _ => "" };
                        Console.WriteLine($"  [OK] {name} (端口 {port}) 已关闭");
                        stopped++;
                    }
                }
                catch { }
            }

            Console.WriteLine();
            if (stopped > 0) Console.WriteLine($"  成功关闭 {stopped} 个服务");
            else Console.WriteLine("  没有运行中的服务");
            Console.WriteLine("========================================");
            Console.WriteLine("按任意键退出...");
            Console.ReadKey();
        }
    }
}
