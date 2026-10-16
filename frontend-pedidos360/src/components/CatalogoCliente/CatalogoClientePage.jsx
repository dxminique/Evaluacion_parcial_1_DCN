import { useEffect, useMemo, useState } from "react";
import { catalogApi } from "../../services/api";
import AppNavigation from "../Navigation/AppNavigation";

const formatoPrecio = new Intl.NumberFormat("es-CL", {
  style: "currency",
  currency: "CLP",
  maximumFractionDigits: 0,
});

const imagenesProducto = [
  { palabras: ["laptop", "thinkpad", "notebook", "computador"], src: "https://images.unsplash.com/photo-1496181133206-80ce9b88a853?auto=format&fit=crop&w=900&q=85" },
  { palabras: ["mouse", "raton"], src: "https://images.unsplash.com/photo-1527814050087-3793815479db?auto=format&fit=crop&w=900&q=85" },
  { palabras: ["monitor", "pantalla"], src: "https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?auto=format&fit=crop&w=900&q=85" },
];

function obtenerImagenProducto(producto) {
  const textoProducto = `${producto.nombre} ${producto.descripcion}`.toLocaleLowerCase("es-CL");
  return imagenesProducto.find(({ palabras }) => palabras.some((palabra) => textoProducto.includes(palabra)))?.src;
}

export default function CatalogoClientePage() {
  const [productos, setProductos] = useState([]);
  const [busqueda, setBusqueda] = useState("");
  const [soloDisponibles, setSoloDisponibles] = useState(false);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let vigente = true;
    catalogApi
      .get("/api/catalog")
      .then(({ data }) => {
        if (vigente) setProductos(data.filter((producto) => producto.activo));
      })
      .catch((err) => {
        if (vigente) setError(err.friendlyMessage || "No pudimos cargar el catálogo. Intenta nuevamente.");
      })
      .finally(() => {
        if (vigente) setCargando(false);
      });
    return () => { vigente = false; };
  }, []);

  const productosVisibles = useMemo(() => {
    const consulta = busqueda.trim().toLocaleLowerCase("es-CL");
    return productos.filter((producto) => {
      const coincide = `${producto.nombre} ${producto.descripcion}`.toLocaleLowerCase("es-CL").includes(consulta);
      return coincide && (!soloDisponibles || producto.stock > 0);
    });
  }, [productos, busqueda, soloDisponibles]);

  return (
    <div className="customer-catalog-page">
      <AppNavigation activePage="catalog" />
      <main className="customer-catalog-main">
        <section className="customer-catalog-hero">
          <div className="customer-catalog-hero-copy">
            <span className="customer-catalog-eyebrow">PEDIDOS360 · CATÁLOGO</span>
            <h1>Encuentra lo que necesitas.</h1>
            <p>Explora nuestros productos y revisa su disponibilidad.</p>
          </div>
          <div className="customer-catalog-hero-art" aria-hidden="true">
            <span className="catalog-art-ring" />
            <span className="catalog-art-box">P<span>360</span></span>
            <span className="catalog-art-dot" />
          </div>
        </section>

        <section className="customer-catalog-content" aria-labelledby="catalog-title">
          <div className="customer-catalog-heading">
            <div>
              <span className="customer-catalog-section-label">SELECCIÓN DISPONIBLE</span>
              <h2 id="catalog-title">Nuestros productos</h2>
            </div>
            {!cargando && !error && <span className="customer-catalog-count">{productos.length} productos</span>}
          </div>

          <div className="customer-catalog-controls">
            <label className="catalog-search">
              <span className="catalog-search-icon" aria-hidden="true">⌕</span>
              <input
                type="search"
                placeholder="Buscar productos..."
                value={busqueda}
                onChange={(event) => setBusqueda(event.target.value)}
                aria-label="Buscar productos"
              />
            </label>
            <label className="catalog-availability-filter">
              <input type="checkbox" checked={soloDisponibles} onChange={(event) => setSoloDisponibles(event.target.checked)} />
              <span>Solo disponibles</span>
            </label>
          </div>

          {cargando && <div className="customer-catalog-message" role="status">Cargando productos…</div>}
          {error && (
            <div className="customer-catalog-message customer-catalog-error" role="alert">
              <p>{error}</p>
              <button type="button" onClick={() => window.location.reload()}>Volver a intentar</button>
            </div>
          )}
          {!cargando && !error && productosVisibles.length > 0 && (
            <div className="customer-product-grid">
              {productosVisibles.map((producto, index) => (
                <article className="customer-product-card" key={producto.id}>
                  <div className={`customer-product-visual product-visual-${index % 3}`} aria-hidden="true">
                    <span className="product-visual-orbit" />
                    <span className="product-visual-initials">{producto.nombre.slice(0, 1).toUpperCase()}</span>
                    <span className="product-visual-index">P · {String(index + 1).padStart(2, "0")}</span>
                    {obtenerImagenProducto(producto) && (
                      <img
                        className="customer-product-image"
                        src={obtenerImagenProducto(producto)}
                        alt=""
                        loading="lazy"
                        onError={(event) => { event.currentTarget.hidden = true; }}
                      />
                    )}
                  </div>
                  <div className="customer-product-info">
                    <div className="customer-product-stock">
                      <span className={producto.stock > 0 ? "stock-indicator" : "stock-indicator stock-indicator-empty"} />
                      {producto.stock > 0 ? "Disponible" : "Agotado"}
                    </div>
                    <h3>{producto.nombre}</h3>
                    <p className="customer-product-description">{producto.descripcion}</p>
                    <div className="customer-product-footer">
                      <span className="customer-product-price">{formatoPrecio.format(Number(producto.precio))}</span>
                      {producto.stock > 0 && <span className="customer-product-units">{producto.stock} unidades</span>}
                    </div>
                  </div>
                </article>
              ))}
            </div>
          )}
          {!cargando && !error && productosVisibles.length === 0 && (
            <div className="customer-catalog-empty">
              <span aria-hidden="true">⌕</span>
              <h3>No encontramos productos</h3>
              <p>Prueba con otro término o cambia los filtros de búsqueda.</p>
              {(busqueda || soloDisponibles) && <button type="button" onClick={() => { setBusqueda(""); setSoloDisponibles(false); }}>Limpiar filtros</button>}
            </div>
          )}
        </section>
      </main>
    </div>
  );
}
