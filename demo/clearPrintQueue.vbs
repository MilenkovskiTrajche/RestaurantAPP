Set UAC = CreateObject("Shell.Application")
UAC.ShellExecute "cmd.exe", "/c net stop spooler && timeout /t 5 && net start spooler", "", "runas", 1
