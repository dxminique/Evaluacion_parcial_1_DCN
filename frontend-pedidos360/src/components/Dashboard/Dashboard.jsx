import { useEffect, useState } from "react";
import { useMsal } from "@azure/msal-react";
import { catalogApi, ordersApi } from "../../services/api";
import AppNavigation from "../Navigation/AppNavigation";
import { Link, Navigate } from "react-router-dom";
import { useRoles } from "../../auth/useRoles";

export default function Dashboard() {
  const { accounts } = useMsal();
  const account = accounts[0];
  const { esCliente, esAdminOOperador } = useRoles();
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
  const fechaActual = new Intl.DateTimeFormat("es-CL", {
    weekday: "long",
    day: "numeric",
    month: "long",
    year: "numeric",
  }).format(new Date());

  if (esCliente) return <Navigate to="/catalogo" replace />;

  return (
    <div className="dashboard-page" style={styles.page}>
      <AppNavigation activePage="dashboard" />
      <main className="dashboard-main" style={styles.main}>
        <div className="dashboard-date">{fechaActual}</div>
        <section className="dashboard-welcome" style={styles.welcomeCard} aria-labelledby="dashboard-title">
          <div className="dashboard-welcome-copy">
            <p className="welcome-eyebrow">CENTRO DE OPERACIONES</p>
            <h1 id="dashboard-title" style={styles.welcomeTitle}>Hola, {account?.name?.split(" ")[0] || "bienvenido"}</h1>
            <p style={styles.welcomeEmail}>Aquí tienes un resumen del estado actual de Pedidos360.</p>
          </div>
          <div className="dashboard-welcome-mark" aria-hidden="true"><span>360</span></div>
        </section>

        <div className="dashboard-stats" style={styles.grid}>
          <article className="dashboard-stat-card dashboard-stat-orders" style={styles.statCard}>
            <div className="dashboard-stat-heading"><span className="dashboard-stat-icon" aria-hidden="true">↗</span><p style={styles.statLabel}>Pedidos</p></div>
            <p style={styles.statValue}>{ordersStatus}</p>
            <span className="dashboard-stat-caption">Actividad registrada</span>
          </article>
          <article className="dashboard-stat-card dashboard-stat-catalog" style={styles.statCard}>
            <div className="dashboard-stat-heading"><span className="dashboard-stat-icon" aria-hidden="true">▦</span><p style={styles.statLabel}>Catálogo</p></div>
            <p style={styles.statValue}>{catalogStatus}</p>
          </article>
          <article className="dashboard-stat-card dashboard-stat-role" style={styles.statCard}>
            <div className="dashboard-stat-heading"><span className="dashboard-stat-icon" aria-hidden="true">◎</span><p style={styles.statLabel}>Tu perfil</p></div>
            <p style={styles.statValue}>{roles}</p>
            <span className="dashboard-stat-caption">Acceso habilitado</span>
          </article>
        </div>

        <section className="dashboard-shortcuts" aria-labelledby="shortcuts-title">
          <div className="dashboard-section-heading">
            <div><p className="page-eyebrow">ACCESO RÁPIDO</p><h2 id="shortcuts-title">¿Qué necesitas hacer?</h2></div>
            <span>Gestiona tu operación en un solo lugar</span>
          </div>
          <div className="dashboard-shortcut-grid">
            {esAdminOOperador ? (
              <Link className="dashboard-shortcut-card" to="/productos">
                <span className="shortcut-icon" aria-hidden="true">＋</span>
                <span><strong>Administrar productos</strong><small>Actualiza precios, stock y catálogo</small></span>
                <span className="shortcut-arrow" aria-hidden="true">→</span>
              </Link>
            ) : (
              <Link className="dashboard-shortcut-card" to="/catalogo">
                <span className="shortcut-icon" aria-hidden="true">⌕</span>
                <span><strong>Explorar catálogo</strong><small>Encuentra productos disponibles</small></span>
                <span className="shortcut-arrow" aria-hidden="true">→</span>
              </Link>
            )}
            <div className="dashboard-shortcut-note"><span aria-hidden="true">✦</span><p><strong>Todo bajo control</strong><small>Consulta el estado de tus servicios desde este panel.</small></p></div>
          </div>
        </section>
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
