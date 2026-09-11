import axios from "axios";
import { msalInstance } from "../auth/msalInstance";
import { loginRequest } from "../auth/authConfig";

const api = axios.create({
  baseURL: "http://localhost:8083",
});

api.interceptors.request.use(async (config) => {
  console.log("1. Interceptor iniciado");
  const account = msalInstance.getAllAccounts()[0];
  console.log("2. Cuenta encontrada:", account);

  if (account) {
    try {
      console.log("3. Pidiendo token...");
      const response = await msalInstance.acquireTokenSilent({
        ...loginRequest,
        account,
      });
      console.log("4. Token obtenido:", response.accessToken.substring(0, 20));
      config.headers.Authorization = `Bearer ${response.accessToken}`;
    } catch (error) {
      console.error("ERROR en acquireTokenSilent:", error);
    }
  }

  console.log("5. Interceptor terminado");
  return config;
});

export default api;