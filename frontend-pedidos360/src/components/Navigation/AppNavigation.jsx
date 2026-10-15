import { useState } from "react";
import { Link } from "react-router-dom";
import { useMsal } from "@azure/msal-react";
import { useRoles } from "../../auth/useRoles";

export default function AppNavigation({ activePage }) {
  const [menuOpen, setMenuOpen] = useState(false);
  const { instance } = useMsal();
  const { esAdminOOperador } = useRoles();
  const closeMenu = () => setMenuOpen(false);

  return (
    <header className="app-header app-navigation">
      <Link className="nav-brand" to="/dashboard" onClick={closeMenu}>
        <img src="/pedidos360-mark.svg" alt="" />
        <span>Pedidos360</span>
      </Link>

      <button
        className={`nav-toggle${menuOpen ? " is-open" : ""}`}
        type="button"
        aria-label={menuOpen ? "Cerrar menú" : "Abrir menú"}
        aria-expanded={menuOpen}
        aria-controls="primary-navigation"
        onClick={() => setMenuOpen((open) => !open)}
      >
        <span />
        <span />
        <span />
      </button>

      <nav id="primary-navigation" className={`app-nav${menuOpen ? " is-open" : ""}`} aria-label="Navegación principal">
        <Link
          to="/dashboard"
          className={activePage === "dashboard" ? "is-active" : ""}
          aria-current={activePage === "dashboard" ? "page" : undefined}
          onClick={closeMenu}
        >
          Inicio
        </Link>
        {esAdminOOperador && (
          <Link
            to="/productos"
            className={activePage === "products" ? "is-active" : ""}
            aria-current={activePage === "products" ? "page" : undefined}
            onClick={closeMenu}
          >
            Productos
          </Link>
        )}
        <button type="button" onClick={() => instance.logoutRedirect()}>
          Cerrar sesión
        </button>
      </nav>
    </header>
  );
}
