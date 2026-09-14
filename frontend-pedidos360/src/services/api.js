import axios from "axios";
import { msalInstance } from "../auth/msalInstance";
import { loginRequest } from "../auth/authConfig";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || "https://kp6zftqlmf.execute-api.us-east-1.amazonaws.com/api",
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

export const catalogApi = axios.create({
  baseURL: import.meta.env.VITE_CATALOG_URL || "https://kp6zftqlmf.execute-api.us-east-1.amazonaws.com/catalog",
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

export default api;