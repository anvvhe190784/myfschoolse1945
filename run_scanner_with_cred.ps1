Add-Type @"
using System;
using System.Runtime.InteropServices;
using System.Text;

public class WinCred {
    [DllImport("advapi32.dll", SetLastError = true, CharSet = CharSet.Unicode)]
    public static extern bool CredRead(string target, int type, int reservedFlag, out IntPtr credentialPtr);

    [DllImport("advapi32.dll", SetLastError = true)]
    public static extern void CredFree(IntPtr credentialPtr);

    [StructLayout(LayoutKind.Sequential, CharSet = CharSet.Unicode)]
    public struct CREDENTIAL {
        public int Flags;
        public int Type;
        public string TargetName;
        public string Comment;
        public long LastWritten;
        public int CredentialBlobSize;
        public IntPtr CredentialBlob;
        public int Persist;
        public int AttributeCount;
        public IntPtr Attributes;
        public string TargetAlias;
        public string UserName;
    }

    public static string GetPassword(string target) {
        IntPtr credPtr;
        if (CredRead(target, 1, 0, out credPtr)) {
            try {
                CREDENTIAL cred = (CREDENTIAL)Marshal.PtrToStructure(credPtr, typeof(CREDENTIAL));
                if (cred.CredentialBlobSize > 0 && cred.CredentialBlob != IntPtr.Zero) {
                    byte[] bytes = new byte[cred.CredentialBlobSize];
                    Marshal.Copy(cred.CredentialBlob, bytes, 0, cred.CredentialBlobSize);
                    return Encoding.UTF8.GetString(bytes);
                }
            } finally {
                CredFree(credPtr);
            }
        }
        return null;
    }
}
"@

$token = [WinCred]::GetPassword("sonarqube-cli/sonarcloud.io:anvvhe190784")
if (-not $token) {
    $token = [WinCred]::GetPassword("LegacyGeneric:target=sonarqube-cli/sonarcloud.io:anvvhe190784")
}

if ($token) {
    Write-Output "Token successfully loaded (length: $($token.Length)). Launching scanner..."
    $env:SONAR_TOKEN = $token
    npx sonarqube-scanner
} else {
    Write-Output "Token not found in credential target"
}
