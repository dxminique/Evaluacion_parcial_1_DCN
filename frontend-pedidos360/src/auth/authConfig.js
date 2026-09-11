export const msalConfig = {
  auth: {
    clientId: "6fc9e12e-949d-4672-8126-89ab30c6f757",
    authority: "https://login.microsoftonline.com/f808e7f8-d340-4129-aa19-44b19281283a",
    redirectUri: "http://localhost:5173",
    navigateToLoginRequestUrl: false,
  },
  cache: {
    cacheLocation: "sessionStorage",
    storeAuthStateInCookie: false,
  },
};

export const loginRequest = {
  scopes: ["api://6fc9e12e-949d-4672-8126-89ab30c6f757/access_as_user"],
};