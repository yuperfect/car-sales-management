using System;
using System.Diagnostics;
using System.Linq;

namespace CarSalesStop
{
    class Program
    {
        static void Main(string[] args)
        {
            Console.WriteLine("========================================");
            Console.WriteLine("  汽车销售管理系统 - 一键关闭");
            Console.WriteLine("========================================");
            Console.WriteLine();
            Console.WriteLine("正在关闭所有服务...");

            int[] ports = { 8080, 5173, 3000 };
            int stopped = 0;

            foreach (int port in ports)
            {
                try
                {
                    var psi = new ProcessStartInfo
                    {
                        FileName = "powershell",
                        Arguments = $"-NoProfile -Command \"$c=Get-NetTCPConnection -LocalPort {port} -ErrorAction SilentlyContinue | Select-Object -First 1; if($c -and $c.OwningProcess -gt 0){{Stop-Process -Id $c.OwningProcess -Force -ErrorAction SilentlyContinue; Write-Host '  [OK] 端口 {port} 已关闭'}} else {{Write-Host '  [..] 端口 {port} 未运行'}}\"",
                        UseShellExecute = false,
                        CreateNoWindow = true,
                        RedirectStandardOutput = true
                    };
                    var proc = Process.Start(psi);
                    string output = proc.StandardOutput.ReadToEnd();
                    proc.WaitForExit(5000);
                    Console.Write(output);
                    stopped++;
                }
                catch { }
            }

            // 也通过窗口标题杀
            try { Process.Start("taskkill", "/f /fi \"WINDOWTITLE eq CarSales-*\"")?.WaitForExit(2000); } catch {}

            Console.WriteLine();
            if (stopped > 0) Console.WriteLine($"  已处理 {stopped} 个端口");
            else Console.WriteLine("  没有运行中的服务");
            Console.WriteLine("========================================");
            Console.WriteLine("按任意键退出...");
            Console.ReadKey();
        }
    }
}
