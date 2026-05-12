import { useEffect, useState, type ChangeEvent } from "react";
import axiosClient from "../../api/axiosClient";
import { Transaction } from "../../types/transaction";
import styles from "./TransactionList.module.scss";
import { toast } from "react-toastify";

const TransactionList = () => {
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string>("");
  const [editingTransactionId, setEditingTransactionId] = useState<
    number | null
  >(null);
  const [editValues, setEditValues] = useState<Partial<Transaction>>({});
  const [showDeleteModal, setShowDeleteModal] = useState<boolean>(false);
  const [transactionToDelete, setTransactionToDelete] = useState<number | null>(
    null
  );

  useEffect(() => {
    fetchTransactions();
  }, []);

  const fetchTransactions = async () => {
    try {
      const response = await axiosClient.get("/transactions");
      setTransactions(response.data);
    } catch (err) {
      console.error("Błąd pobierania transakcji:", err);
      setError("Nie udało się pobrać transakcji.");
    } finally {
      setLoading(false);
    }
  };

  const startEdit = (transaction: Transaction) => {
    setEditingTransactionId(transaction.id);
    setEditValues({ ...transaction });
  };

  const handleEditChange = (
    e: ChangeEvent<HTMLInputElement | HTMLSelectElement>,
    field: keyof Transaction
  ) => {
    setEditValues((prev) => ({ ...prev, [field]: e.target.value }));
  };

  const updateTransaction = async () => {
    if (!editingTransactionId || !editValues) return;

    try {
      const response = await axiosClient.put(
        `/transactions/${editingTransactionId}`,
        editValues
      );

      if (response.status === 200) {
        setTransactions(
          transactions.map((t) =>
            t.id === editingTransactionId ? { ...t, ...editValues } : t
          )
        );
        setEditingTransactionId(null);
        toast.success("✅ Transakcja zaktualizowana!");
      } else {
        toast.error(" Nie udało się edytować transakcji.");
      }
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } catch (error: any) {
      console.error("Błąd edycji:", error);

      if (error.response && error.response.data) {
        const errorMessage =
          typeof error.response.data === "string"
            ? error.response.data
            : Object.values(error.response.data).join(" ");
        toast.error(` ${errorMessage}`);
      } else {
        toast.error("Błąd podczas edycji transakcji.");
      }
    }
  };
  const openDeleteModal = (id: number) => {
    setTransactionToDelete(id);
    setShowDeleteModal(true);
  };

  const deleteTransaction = async () => {
    if (!transactionToDelete) return;

    try {
      const response = await axiosClient.delete(
        `/transactions/${transactionToDelete}`
      );

      // `204` oznacza sukces, ale nie zawiera odpowiedzi, więc sprawdzamy status
      if (response.status === 204 || response.status === 200) {
        setTransactions(
          transactions.filter((t) => t.id !== transactionToDelete)
        );
        toast.success(" Transakcja usunięta!");
      } else {
        toast.error(" Nie udało się usunąć transakcji.");
      }
    } catch (error) {
      console.error("Błąd usuwania:", error);
      toast.error(" Błąd podczas usuwania transakcji.");
    } finally {
      setShowDeleteModal(false);
      setTransactionToDelete(null);
    }
  };

  if (loading) return <p>Ładowanie...</p>;
  if (error) return <p className="text-red-500">{error}</p>;

  return (
    <div className={styles["transaction-list"]}>
      <h2>Lista Transakcji</h2>
      <table>
        <thead>
          <tr>
            <th>Kwota</th>
            <th>Typ</th>
            <th>Tagi</th>
            <th>Notatki</th>
            <th>Akcje</th>
          </tr>
        </thead>
        <tbody>
          {transactions.map((transaction) => (
            <tr key={transaction.id}>
              <td>
                {editingTransactionId === transaction.id ? (
                  <input
                    type="number"
                    value={editValues.amount || ""}
                    onChange={(e) => handleEditChange(e, "amount")}
                  />
                ) : (
                  `${transaction.amount} zł`
                )}
              </td>
              <td>
                {editingTransactionId === transaction.id ? (
                  <select
                    value={editValues.type || ""}
                    onChange={(e) => handleEditChange(e, "type")}
                  >
                    <option value="INCOME">Przychód</option>
                    <option value="EXPENSE">Wydatek</option>
                  </select>
                ) : transaction.type === "INCOME" ? (
                  "Przychód"
                ) : (
                  "Wydatek"
                )}
              </td>

              <td>
                {editingTransactionId === transaction.id ? (
                  <input
                    type="text"
                    value={editValues.tags || ""}
                    onChange={(e) => handleEditChange(e, "tags")}
                  />
                ) : (
                  transaction.tags
                )}
              </td>
              <td>
                {editingTransactionId === transaction.id ? (
                  <input
                    type="text"
                    value={editValues.notes || ""}
                    onChange={(e) => handleEditChange(e, "notes")}
                  />
                ) : (
                  transaction.notes
                )}
              </td>
              <td>
                {editingTransactionId === transaction.id ? (
                  <>
                    <button className={styles.save} onClick={updateTransaction}>
                      Zapisz
                    </button>
                    <button
                      className={styles.cancel}
                      onClick={() => setEditingTransactionId(null)}
                    >
                      Anuluj
                    </button>
                  </>
                ) : (
                  <>
                    <button
                      className={styles.edit}
                      onClick={() => startEdit(transaction)}
                    >
                      Edytuj
                    </button>
                    <button
                      className={styles.delete}
                      onClick={() => openDeleteModal(transaction.id)}
                    >
                      Usuń
                    </button>
                  </>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {showDeleteModal && (
        <div className={styles["modal"]}>
          <div className={styles["modal-content"]}>
            <p>Czy na pewno chcesz usunąć transakcję?</p>
            <div className={styles["modal-buttons"]}>
              <button className={styles["confirm"]} onClick={deleteTransaction}>
                Tak
              </button>
              <button
                className={styles["cancel"]}
                onClick={() => setShowDeleteModal(false)}
              >
                Anuluj
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default TransactionList;
