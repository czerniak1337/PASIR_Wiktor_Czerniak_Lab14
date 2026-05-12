// src/pages/LoginPage/LoginPage.tsx
import { useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { authApi } from '../../api/authApi';
import ErrorMessage from '../../components/ErrorMessage/ErrorMessage';
import styles from './Login.Page.module.scss';

const LoginPage = () => {
  const navigate = useNavigate();
  const [email, setEmail] = useState<string>('');
  const [password, setPassword] = useState<string>('');
  const [error, setError] = useState<string>('');

  const handleLogin = async (e: FormEvent) => {
    e.preventDefault();
    setError(''); // Reset błędu
    try {
      const data = await authApi.login({ email, password });
      localStorage.setItem('accessToken', data.token);
      navigate('/transactions');
    } catch (error: unknown) {
      setError('Błąd logowania. Sprawdź swoje dane.' + error);
    }
  };

  return (
    <div className={styles.container}>
      <h2 className={styles.title}>Logowanie</h2>
      <form onSubmit={handleLogin} className={styles.form}>
        <div className={styles.formGroup}>
          <label>Email:</label>
          <input 
            type="email" 
            value={email} 
            onChange={(e) => setEmail(e.target.value)} 
            required 
            className={styles.input}
          />
          {error && <ErrorMessage message={error} />}
        </div>
        <div className={styles.formGroup}>
          <label>Hasło:</label>
          <input 
            type="password" 
            value={password} 
            onChange={(e) => setPassword(e.target.value)} 
            required 
            className={styles.input}
          />
        </div>
        <button type="submit" className={styles.button}>Zaloguj</button>
      </form>
    </div>
  );
};

export default LoginPage;
