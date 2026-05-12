import { useForm } from "react-hook-form";
import { TransactionType } from "../../types/transaction";
import axiosClient from "../../api/axiosClient";
import styles from "./TransactionForm.module.scss";
import { toast } from "react-toastify";

interface FormData {
  amount: number;
  type: TransactionType;
  tags: string;
  notes: string;
}

const TransactionForm = () => {
  const { register, handleSubmit, reset } = useForm<FormData>();

  const onSubmit = async (data: FormData) => {
    try {
      await axiosClient.post("/transactions", data);
      toast.success("✅ Transakcja dodana!");
      reset();
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } catch (error: any) {
      if (error.response && error.response.status === 400) {
        Object.values(error.response.data).forEach((msg) => {
          toast.error(` ${msg}`);
        });
      } else {
        toast.error(" Błąd podczas dodawania transakcji.");
      }
    }
  };

  return (
    <form
      onSubmit={handleSubmit(onSubmit)}
      className={styles["transaction-form"]}
    >
      <h2>Dodaj Transakcję</h2>

      <label>Kwota:</label>
      <input
        {...register("amount", { required: true })}
        type="number"
        required
      />

      <label>Typ:</label>
      <select {...register("type", { required: true })} required>
        <option value="INCOME">Przychód</option>
        <option value="EXPENSE">Wydatek</option>
      </select>

      <label>Tagi:</label>
      <input {...register("tags", { required: true })} type="text" required />

      <label>Notatki:</label>
      <input {...register("notes")} type="text" />

      <button type="submit">Dodaj</button>
    </form>
  );
};

export default TransactionForm;
