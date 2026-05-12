import { Link } from "react-router-dom";
import styles from "./Navbar.module.scss";

const Navbar = () => {
  return (
    <nav className={styles.navbar}>
      <h1 className={styles.logo}>
        <Link to="/">💰 Budget App</Link>
      </h1>
      <ul className={styles["nav-list"]}>
        <div className={`${styles["nav-container"]}`}>
          <li><Link to="/add-transaction">Dodaj Transakcję</Link></li>
          <li><Link to="/transactions">Lista Transakcji</Link></li>
        </div>
        <div className={`${styles["nav-container"]}`}>
          <li><Link to="/login">Logowanie</Link></li>
          <li><Link to="/register">Rejestracja</Link></li>
        </div>
      </ul>
    </nav>
  );
};

export default Navbar;
