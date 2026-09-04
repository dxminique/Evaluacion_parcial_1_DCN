import { useMsal } from "@azure/msal-react";
import { loginRequest } from "../../auth/authConfig";

export default function Login() {
  const { instance } = useMsal();

  const handleLogin = () => {
    instance.loginRedirect(loginRequest).catch((error) => {
      console.error(error);
    });
  };

  return (
    <div style={styles.container}>
      <div style={styles.card}>
        <h1 style={styles.title}>Pedidos360</h1>
        <p style={styles.subtitle}>Inicia sesión para continuar</p>
        <button style={styles.button} onClick={handleLogin}>
          Iniciar sesión con Microsoft
        </button>
      </div>
    </div>
  );
}

const styles = {
  container: {
    display: "flex",
    justifyContent: "center",
    alignItems: "center",
    height: "100vh",
    backgroundColor: "#1e1e2f",
  },
  card: {
    backgroundColor: "#2a2a3d",
    padding: "48px 40px",
    borderRadius: "12px",
    textAlign: "center",
    boxShadow: "0 4px 20px rgba(0,0,0,0.3)",
  },
  title: {
    color: "#fff",
    fontSize: "32px",
    marginBottom: "8px",
  },
  subtitle: {
    color: "#aaa",
    marginBottom: "24px",
  },
  button: {
    backgroundColor: "#0078d4",
    color: "#fff",
    border: "none",
    padding: "12px 24px",
    borderRadius: "6px",
    fontSize: "16px",
    cursor: "pointer",
  },
};