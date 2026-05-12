// src/api/authApi.ts
import axiosClient from './axiosClient';

interface RegisterData {
  login: string;
  email: string;
  password: string;
}

interface LoginData {
  email: string;
  password: string;
}

interface AuthResponse {
  token: string;
  // Dodaj inne pola odpowiedzi, jeśli są
}

export const authApi = {
  register: (data: RegisterData): Promise<AuthResponse> => {
    return axiosClient.post('/register', data);
  },
  login: (data: LoginData): Promise<AuthResponse> => {
    return axiosClient.post('/login', data);
  },
};
