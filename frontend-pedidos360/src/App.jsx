import { BrowserRouter, Routes, Route } from "react-router-dom";
import Login from "./components/Login/Login";
import Dashboard from "./components/Dashboard/Dashboard";
import ProtectedRoute from "./guards/ProtectedRoute";
import RoleRoute from "./guards/RoleRoute";
import ProductosPage from "./pages/Productos/ProductosPage";
import CatalogoClientePage from "./components/CatalogoCliente/CatalogoClientePage";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route
          path="/dashboard"
          element={
            <ProtectedRoute>
              <Dashboard />
            </ProtectedRoute>
          }
        />
        <Route
          path="/"
          element={
            <ProtectedRoute>
              <Dashboard />
            </ProtectedRoute>
          }
        />
        <Route
          path="/catalogo"
          element={
            <RoleRoute allowedRoles={["Cliente"]}>
              <CatalogoClientePage />
            </RoleRoute>
          }
        />
        <Route
          path="/productos"
          element={
            <RoleRoute allowedRoles={["Admin", "Operador"]}>
              <ProductosPage />
            </RoleRoute>
          }
        />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
