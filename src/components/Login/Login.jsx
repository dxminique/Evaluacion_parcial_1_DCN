import { useMsal } from "@azure/msal-react";
import { loginRequest } from "../../auth/authConfig";
import logo from "../../assets/logo.png";

export default function Login() {
  const { instance } = useMsal();

  const handleLogin = () => {
    instance.loginRedirect(loginRequest).catch((error) => {
      console.error(error);
    });
  };

  return (
    <div style={styles.page}>
      <div style={styles.grid}>
        <div style={styles.brandPanel}>
          <div style={styles.logoBox}>
            <img src={logo} alt="Pedidos360" style={styles.logoImg} />
          </div>
          <h1 style={styles.brandTitle}>Pedidos360</h1>
          <p style={styles.brandText}>
            Gestión de pedidos en tiempo real, conectada a tu cuenta corporativa.
          </p>
        </div>
        <div style={styles.formPanel}>
          <div style={styles.formInner}>
            <h2 style={styles.formTitle}>Inicia sesión</h2>
            <p style={styles.formSubtitle}>Usa tu cuenta corporativa para continuar.</p>
            <button style={styles.msButton} onClick={handleLogin}>
              <svg width="18" height="18" viewBox="0 0 21 21" aria-hidden="true">
                <rect x="1" y="1" width="9" height="9" fill="#f25022" />
                <rect x="11" y="1" width="9" height="9" fill="#7fba00" />
                <rect x="1" y="11" width="9" height="9" fill="#00a4ef" />
                <rect x="11" y="11" width="9" height="9" fill="#ffb900" />
              </svg>
              Iniciar sesión con Microsoft
            </button>
            <p style={styles.footerText}>Autenticación gestionada por Azure AD</p>
          </div>
        </div>
      </div>
    </div>
  );
}

const styles = {
  page: {
    display: "flex",
    justifyContent: "center",
    alignItems: "center",
    minHeight: "100vh",
    backgroundColor: "#0f0f14",
    padding: "24px",
  },
  grid: {
    display: "grid",
    gridTemplateColumns: "1fr 1fr",
    width: "100%",
    maxWidth: "900px",
    minHeight: "480px",
    borderRadius: "12px",
    overflow: "hidden",
    boxShadow: "0 8px 30px rgba(0,0,0,0.4)",
  },
  brandPanel: {
    backgroundColor: "#1c1c26",
    display: "flex",
    flexDirection: "column",
    justifyContent: "center",
    padding: "48px",
  },
  logoBox: {
    width: "64px",
    height: "64px",
    borderRadius: "10px",
    backgroundColor: "#fff",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    marginBottom: "24px",
  },
  logoImg: {
    width: "40px",
    height: "40px",
  },
  brandTitle: {
    color: "#fff",
    fontSize: "26px",
    fontWeight: 600,
    margin: "0 0 12px",
  },
  brandText: {
    color: "#9a9aa8",
    fontSize: "15px",
    lineHeight: 1.6,
    margin: 0,
  },
  formPanel: {
    backgroundColor: "#25252f",
    display: "flex",
    justifyContent: "center",
    alignItems: "center",
    padding: "48px",
  },
  formInner: {
    width: "100%",
    maxWidth: "280px",
  },
  formTitle: {
    color: "#fff",
    fontSize: "20px",
    fontWeight: 600,
    margin: "0 0 6px",
  },
  formSubtitle: {
    color: "#9a9aa8",
    fontSize: "14px",
    margin: "0 0 28px",
  },
  msButton: {
    width: "100%",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    gap: "10px",
    height: "44px",
    backgroundColor: "#fff",
    color: "#1a1a1a",
    border: "none",
    borderRadius: "6px",
    fontSize: "14px",
    fontWeight: 500,
    cursor: "pointer",
  },
  footerText: {
    color: "#6a6a76",
    fontSize: "12px",
    textAlign: "center",
    marginTop: "16px",
  },
};