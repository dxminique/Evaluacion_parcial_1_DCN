import axios from "axios";
import { msalInstance } from "../auth/msalInstance";
import { loginRequest } from "../auth/authConfig";

const api = axios.create({
  baseURL: "http://localhost:8080", // URL del BFF (Spring Boot)
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

export default api;