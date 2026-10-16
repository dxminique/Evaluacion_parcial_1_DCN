import axios from "axios";
import { msalInstance } from "../auth/msalInstance";
import { loginRequest } from "../auth/authConfig";

function attachForbiddenMessage(instance) {
  instance.interceptors.response.use(
    (response) => response,
    (error) => {
      if (error.response?.status === 403) {
        error.friendlyMessage = "No tienes permisos para esta acción";
      }
      return Promise.reject(error);
    }
  );
}

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || (import.meta.env.DEV ? "http://localhost:8083" : "https://kp6zftqlmf.execute-api.us-east-1.amazonaws.com/api"),
});

api.interceptors.request.use(async (config) => {
  const account = msalInstance.getAllAccounts()[0];

  if (account) {
    const response = await msalInstance.acquireTokenSilent({
      ...loginRequest,
      account,
    });
    config.headers.Authorization = `Bearer ${response.accessToken}`;
  }

  return config;
});

attachForbiddenMessage(api);

export const catalogApi = axios.create({
  baseURL: import.meta.env.VITE_CATALOG_URL || (import.meta.env.DEV ? "http://localhost:8086" : "https://kp6zftqlmf.execute-api.us-east-1.amazonaws.com/catalog"),
});

catalogApi.interceptors.request.use(async (config) => {
  const account = msalInstance.getAllAccounts()[0];

  if (account) {
    const response = await msalInstance.acquireTokenSilent({
      ...loginRequest,
      account,
    });
    config.headers.Authorization = `Bearer ${response.accessToken}`;
  }

  return config;
});

attachForbiddenMessage(catalogApi);

export const ordersApi = axios.create({
  baseURL: import.meta.env.VITE_ORDERS_URL || (import.meta.env.DEV ? "http://localhost:8087" : "https://kp6zftqlmf.execute-api.us-east-1.amazonaws.com/orders"),
});

ordersApi.interceptors.request.use(async (config) => {
  const account = msalInstance.getAllAccounts()[0];

  if (account) {
    const response = await msalInstance.acquireTokenSilent({
      ...loginRequest,
      account,
    });
    config.headers.Authorization = `Bearer ${response.accessToken}`;
  }

  return config;
});

attachForbiddenMessage(ordersApi);

export default api;
