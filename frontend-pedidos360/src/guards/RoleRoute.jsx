import { Navigate } from "react-router-dom";
import ProtectedRoute from "./ProtectedRoute";
import { useRoles } from "../auth/useRoles";

function RoleCheck({ allowedRoles, children }) {
  const { roles } = useRoles();
  const tienePermiso = roles.some((rol) => allowedRoles.includes(rol));

  if (!tienePermiso) {
    return <Navigate to="/dashboard" replace />;
  }

  return children;
}

export default function RoleRoute({ allowedRoles, children }) {
  return (
    <ProtectedRoute>
      <RoleCheck allowedRoles={allowedRoles}>{children}</RoleCheck>
    </ProtectedRoute>
  );
}
