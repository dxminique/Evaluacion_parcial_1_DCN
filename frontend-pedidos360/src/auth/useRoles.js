import { useMsal } from "@azure/msal-react";

export function useRoles() {
  const { accounts } = useMsal();
  const account = accounts[0];
  const roles = account?.idTokenClaims?.roles ?? [];
  const esAdminOOperador = roles.includes("Admin") || roles.includes("Operador");
  const esCliente = roles.includes("Cliente");

  return { roles, esAdminOOperador, esCliente };
}
