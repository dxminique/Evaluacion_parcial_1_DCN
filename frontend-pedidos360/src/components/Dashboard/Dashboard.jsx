import { useEffect, useState } from "react";
import { useMsal } from "@azure/msal-react";
import { catalogApi, ordersApi } from "../../services/api";
import AppNavigation from "../Navigation/AppNavigation";
import { Navigate } from "react-router-dom";
import { useRoles } from "../../auth/useRoles";

export default function Dashboard() {
  const { accounts } = useMsal();
  const account = accounts[0];
  const { esCliente } = useRoles();
  const [catalogStatus, setCatalogStatus] = useState("—");
  const [ordersStatus, setOrdersStatus] = useState("—");

  useEffect(() => {
    catalogApi
      .get("/api/catalog")
      .then((response) => {
        setCatalogStatus(`${response.data.length} productos en catálogo`);
      })
      .catch((error) => {
        console.error(error);
        setCatalogStatus(error.friendlyMessage || "No disponible");
      });
  }, []);

  useEffect(() => {
    ordersApi
      .get("/api/orders")
      .then((response) => {
        setOrdersStatus(`${response.data.length} pedidos activos`);
      })
      .catch((error) => {
        console.error(error);
        setOrdersStatus(error.friendlyMessage || "No disponible");
      });
  }, []);

  const roles = account?.idTokenClaims?.roles?.join(", ") || "Sin rol asignado";

  if (esCliente) return <Navigate to="/catalogo" replace />;

  return (
    <div className="dashboard-page" style={styles.page}>
      <AppNavigation activePage="dashboard" />
      <main className="dashboard-main" style={styles.main}>
        <div className="dashboard-welcome" style={styles.welcomeCard}>
          <p className="welcome-eyebrow">RESUMEN DE OPERACIONES</p>
          <h1 style={styles.welcomeTitle}>Bienvenido, {account?.name}</h1>
          <p style={styles.welcomeEmail}>{account?.username} · Este es el estado actual de tu operación.</p>
        </div>

        <div className="dashboard-stats" style={styles.grid}>
          <div className="dashboard-stat-card" style={styles.statCard}>
            <p style={styles.statLabel}>Pedidos activos</p>
            <p style={styles.statValue}>{ordersStatus}</p>
          </div>
          <div className="dashboard-stat-card" style={styles.statCard}>
            <p style={styles.statLabel}>Stock crítico</p>
            <p style={styles.statValue}>{catalogStatus}</p>
          </div>
          <div className="dashboard-stat-card" style={styles.statCard}>
            <p style={styles.statLabel}>Rol</p>
            <p style={styles.statValue}>{roles}</p>
          </div>
        </div>
      </main>
    </div>
  );
}

const styles = {
  page: {
    minHeight: "100vh",
    backgroundColor: "#0B1F33",
  },
  header: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    padding: "16px 32px",
    backgroundColor: "#123A5A",
    borderBottom: "1px solid #1E4F73",
  },
  headerLeft: {
    display: "flex",
    alignItems: "center",
    gap: "12px",
  },
  headerLogo: {
    width: "28px",
    height: "28px",
  },
  headerTitle: {
    color: "#fff",
    fontSize: "16px",
    fontWeight: 600,
  },
  headerRight: {
    display: "flex",
    alignItems: "center",
    gap: "16px",
  },
  manageLink: {
    color: "#fff",
    fontSize: "13px",
    textDecoration: "none",
    border: "1px solid #1E4F73",
    borderRadius: "6px",
    padding: "8px 16px",
  },
  avatar: {
    width: "36px",
    height: "36px",
    borderRadius: "50%",
    backgroundColor: "#0078d4",
    color: "#fff",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    fontSize: "13px",
    fontWeight: 600,
  },
  logoutButton: {
    backgroundColor: "transparent",
    color: "#e5484d",
    border: "1px solid #3a3a46",
    borderRadius: "6px",
    padding: "8px 16px",
    fontSize: "13px",
    cursor: "pointer",
  },
  main: {
    padding: "32px",
    maxWidth: "900px",
    margin: "0 auto",
  },
  welcomeCard: {
    backgroundColor: "#123A5A",
    borderRadius: "12px",
    padding: "24px 28px",
    marginBottom: "24px",
  },
  welcomeTitle: {
    color: "#fff",
    fontSize: "20px",
    fontWeight: 600,
    margin: "0 0 4px",
  },
  welcomeEmail: {
    color: "#9a9aa8",
    fontSize: "14px",
    margin: 0,
  },
  grid: {
    display: "grid",
    gridTemplateColumns: "repeat(3, 1fr)",
    gap: "16px",
  },
  statCard: {
    backgroundColor: "#123A5A",
    borderRadius: "12px",
    padding: "20px",
  },
  statLabel: {
    color: "#9a9aa8",
    fontSize: "13px",
    margin: "0 0 8px",
  },
  statValue: {
    color: "#fff",
    fontSize: "24px",
    fontWeight: 600,
    margin: 0,
  },
};
