import { useMsal } from "@azure/msal-react";

export default function Dashboard() {
  const { accounts, instance } = useMsal();
  const account = accounts[0];

  const handleLogout = () => {
    instance.logoutRedirect();
  };

  return (
    <div style={styles.container}>
      <div style={styles.card}>
        <h1 style={styles.title}>Pedidos360</h1>
        <p style={styles.welcome}>Bienvenido, {account?.name}</p>
        <p style={styles.email}>{account?.username}</p>
        <button style={styles.button} onClick={handleLogout}>
          Cerrar sesión
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
    minWidth: "320px",
  },
  title: {
    color: "#fff",
    fontSize: "28px",
    marginBottom: "16px",
  },
  welcome: {
    color: "#fff",
    fontSize: "18px",
    marginBottom: "4px",
  },
  email: {
    color: "#aaa",
    fontSize: "14px",
    marginBottom: "24px",
  },
  button: {
    backgroundColor: "#d13438",
    color: "#fff",
    border: "none",
    padding: "12px 24px",
    borderRadius: "6px",
    fontSize: "16px",
    cursor: "pointer",
  },
};