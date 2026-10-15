import { useEffect, useState } from "react";
import { catalogApi } from "../../services/api";
import AppNavigation from "../../components/Navigation/AppNavigation";

const initialForm = { nombre: "", descripcion: "", precio: "", stock: "" };

export default function ProductosPage() {
  const [productos, setProductos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const [form, setForm] = useState(initialForm);
  const [creando, setCreando] = useState(false);

  const [editId, setEditId] = useState(null);
  const [editForm, setEditForm] = useState(initialForm);
  const [editActivo, setEditActivo] = useState(true);
  const [guardando, setGuardando] = useState(false);

  const [eliminandoId, setEliminandoId] = useState(null);

  const cargarProductos = () => {
    setLoading(true);
    catalogApi
      .get("/api/catalog")
      .then((response) => setProductos(response.data))
      .catch((err) => {
        setError(err.friendlyMessage || "No se pudieron cargar los productos.");
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    cargarProductos();
  }, []);

  const limpiarMensajes = () => {
    setError("");
    setSuccess("");
  };

  const handleCrear = async (e) => {
    e.preventDefault();
    limpiarMensajes();
    setCreando(true);
    try {
      await catalogApi.post("/api/catalog", {
        nombre: form.nombre,
        descripcion: form.descripcion,
        precio: Number(form.precio),
        stock: Number(form.stock),
      });
      setForm(initialForm);
      setSuccess("Producto creado correctamente.");
      cargarProductos();
    } catch (err) {
      setError(err.friendlyMessage || err.response?.data?.error || "No se pudo crear el producto.");
    } finally {
      setCreando(false);
    }
  };

  const iniciarEdicion = (producto) => {
    limpiarMensajes();
    setEditId(producto.id);
    setEditForm({
      nombre: producto.nombre,
      descripcion: producto.descripcion,
      precio: String(producto.precio),
      stock: String(producto.stock),
    });
    setEditActivo(Boolean(producto.activo));
  };

  const cancelarEdicion = () => {
    setEditId(null);
    setEditForm(initialForm);
  };

  const handleGuardarEdicion = async (id) => {
    limpiarMensajes();
    setGuardando(true);
    try {
      await catalogApi.put(`/api/catalog/${id}`, {
        nombre: editForm.nombre,
        descripcion: editForm.descripcion,
        precio: Number(editForm.precio),
        stock: Number(editForm.stock),
        activo: editActivo,
      });
      setSuccess("Producto actualizado correctamente.");
      setEditId(null);
      cargarProductos();
    } catch (err) {
      setError(err.friendlyMessage || err.response?.data?.error || "No se pudo actualizar el producto.");
    } finally {
      setGuardando(false);
    }
  };

  const handleEliminar = async (producto) => {
    const confirmado = window.confirm(`¿Eliminar "${producto.nombre}"? Esta acción no se puede deshacer.`);
    if (!confirmado) return;

    limpiarMensajes();
    setEliminandoId(producto.id);
    try {
      await catalogApi.delete(`/api/catalog/${producto.id}`);
      setSuccess("Producto eliminado correctamente.");
      cargarProductos();
    } catch (err) {
      setError(err.friendlyMessage || err.response?.data?.error || "No se pudo eliminar el producto.");
    } finally {
      setEliminandoId(null);
    }
  };

  return (
    <div className="products-page" style={styles.page}>
      <AppNavigation activePage="products" />
      <main className="products-main" style={styles.main}>
        {error && <div className="status-alert status-alert-error" style={styles.errorBox}>{error}</div>}
        {success && <div className="status-alert status-alert-success" style={styles.successBox}>{success}</div>}

        <section className="product-card" style={styles.card}>
          <h2 style={styles.cardTitle}>Nuevo producto</h2>
          <form className="product-create-form" onSubmit={handleCrear} style={styles.form}>
            <input
              style={styles.input}
              placeholder="Nombre"
              value={form.nombre}
              onChange={(e) => setForm({ ...form, nombre: e.target.value })}
              required
            />
            <input
              style={styles.input}
              placeholder="Descripción"
              value={form.descripcion}
              onChange={(e) => setForm({ ...form, descripcion: e.target.value })}
              required
            />
            <input
              style={styles.input}
              type="number"
              min="0.01"
              step="0.01"
              placeholder="Precio"
              value={form.precio}
              onChange={(e) => setForm({ ...form, precio: e.target.value })}
              required
            />
            <input
              style={styles.input}
              type="number"
              min="0"
              step="1"
              placeholder="Stock"
              value={form.stock}
              onChange={(e) => setForm({ ...form, stock: e.target.value })}
              required
            />
            <button type="submit" style={styles.primaryButton} disabled={creando}>
              {creando ? "Creando..." : "Crear producto"}
            </button>
          </form>
        </section>

        <section className="product-card catalog-card" style={styles.card}>
          <h2 style={styles.cardTitle}>Catálogo</h2>
          {loading ? (
            <p style={styles.mutedText}>Cargando productos...</p>
          ) : (
            <table className="catalog-table" style={styles.table}>
              <thead>
                <tr>
                  <th style={styles.th}>ID</th>
                  <th style={styles.th}>Nombre</th>
                  <th style={styles.th}>Descripción</th>
                  <th style={styles.th}>Precio</th>
                  <th style={styles.th}>Stock</th>
                  <th style={styles.th}>Activo</th>
                  <th style={styles.th}>Acciones</th>
                </tr>
              </thead>
              <tbody>
                {productos.map((producto) =>
                  editId === producto.id ? (
                    <tr key={producto.id}>
                      <td style={styles.td}>{producto.id}</td>
                      <td style={styles.td}>
                        <input
                          style={styles.inputSmall}
                          value={editForm.nombre}
                          onChange={(e) => setEditForm({ ...editForm, nombre: e.target.value })}
                        />
                      </td>
                      <td style={styles.td}>
                        <input
                          style={styles.inputSmall}
                          value={editForm.descripcion}
                          onChange={(e) => setEditForm({ ...editForm, descripcion: e.target.value })}
                        />
                      </td>
                      <td style={styles.td}>
                        <input
                          style={styles.inputSmall}
                          type="number"
                          min="0.01"
                          step="0.01"
                          value={editForm.precio}
                          onChange={(e) => setEditForm({ ...editForm, precio: e.target.value })}
                        />
                      </td>
                      <td style={styles.td}>
                        <input
                          style={styles.inputSmall}
                          type="number"
                          min="0"
                          step="1"
                          value={editForm.stock}
                          onChange={(e) => setEditForm({ ...editForm, stock: e.target.value })}
                        />
                      </td>
                      <td style={styles.td}>
                        <input
                          type="checkbox"
                          checked={editActivo}
                          onChange={(e) => setEditActivo(e.target.checked)}
                        />
                      </td>
                      <td style={styles.td}>
                        <button
                          style={styles.linkButton}
                          onClick={() => handleGuardarEdicion(producto.id)}
                          disabled={guardando}
                        >
                          {guardando ? "Guardando..." : "Guardar"}
                        </button>
                        <button style={styles.linkButtonMuted} onClick={cancelarEdicion}>
                          Cancelar
                        </button>
                      </td>
                    </tr>
                  ) : (
                    <tr key={producto.id}>
                      <td style={styles.td}>{producto.id}</td>
                      <td style={styles.td}>{producto.nombre}</td>
                      <td style={styles.td}>{producto.descripcion}</td>
                      <td style={styles.td}>${producto.precio}</td>
                      <td style={styles.td}>{producto.stock}</td>
                      <td style={styles.td}>{producto.activo ? "Sí" : "No"}</td>
                      <td style={styles.td}>
                        <button style={styles.linkButton} onClick={() => iniciarEdicion(producto)}>
                          Editar
                        </button>
                        <button
                          style={styles.linkButtonDanger}
                          onClick={() => handleEliminar(producto)}
                          disabled={eliminandoId === producto.id}
                        >
                          {eliminandoId === producto.id ? "Eliminando..." : "Eliminar"}
                        </button>
                      </td>
                    </tr>
                  )
                )}
                {productos.length === 0 && (
                  <tr>
                    <td style={styles.td} colSpan={7}>
                      No hay productos registrados.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          )}
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
  backLink: {
    color: "#fff",
    fontSize: "13px",
    textDecoration: "none",
    border: "1px solid #1E4F73",
    borderRadius: "6px",
    padding: "8px 16px",
  },
  main: {
    padding: "32px",
    maxWidth: "1000px",
    margin: "0 auto",
  },
  errorBox: {
    backgroundColor: "#3a1c1f",
    border: "1px solid #e5484d",
    color: "#ff9a9e",
    borderRadius: "8px",
    padding: "12px 16px",
    marginBottom: "16px",
    fontSize: "14px",
  },
  successBox: {
    backgroundColor: "#15321f",
    border: "1px solid #3fb950",
    color: "#8ee6a1",
    borderRadius: "8px",
    padding: "12px 16px",
    marginBottom: "16px",
    fontSize: "14px",
  },
  card: {
    backgroundColor: "#123A5A",
    borderRadius: "12px",
    padding: "24px 28px",
    marginBottom: "24px",
  },
  cardTitle: {
    color: "#fff",
    fontSize: "18px",
    fontWeight: 600,
    margin: "0 0 16px",
  },
  form: {
    display: "flex",
    flexWrap: "wrap",
    gap: "12px",
    alignItems: "center",
  },
  input: {
    flex: "1 1 160px",
    backgroundColor: "#0B1F33",
    border: "1px solid #1E4F73",
    borderRadius: "6px",
    padding: "10px 12px",
    color: "#fff",
    fontSize: "14px",
  },
  inputSmall: {
    width: "100%",
    backgroundColor: "#0B1F33",
    border: "1px solid #1E4F73",
    borderRadius: "6px",
    padding: "6px 8px",
    color: "#fff",
    fontSize: "13px",
  },
  primaryButton: {
    backgroundColor: "#0078d4",
    color: "#fff",
    border: "none",
    borderRadius: "6px",
    padding: "10px 20px",
    fontSize: "14px",
    fontWeight: 500,
    cursor: "pointer",
  },
  mutedText: {
    color: "#9a9aa8",
    fontSize: "14px",
  },
  table: {
    width: "100%",
    borderCollapse: "collapse",
  },
  th: {
    textAlign: "left",
    color: "#9a9aa8",
    fontSize: "12px",
    textTransform: "uppercase",
    borderBottom: "1px solid #1E4F73",
    padding: "8px 10px",
  },
  td: {
    color: "#fff",
    fontSize: "14px",
    borderBottom: "1px solid #1E4F73",
    padding: "10px",
  },
  linkButton: {
    backgroundColor: "transparent",
    color: "#58a6ff",
    border: "none",
    fontSize: "13px",
    cursor: "pointer",
    marginRight: "12px",
    padding: 0,
  },
  linkButtonMuted: {
    backgroundColor: "transparent",
    color: "#9a9aa8",
    border: "none",
    fontSize: "13px",
    cursor: "pointer",
    padding: 0,
  },
  linkButtonDanger: {
    backgroundColor: "transparent",
    color: "#e5484d",
    border: "none",
    fontSize: "13px",
    cursor: "pointer",
    padding: 0,
  },
};
