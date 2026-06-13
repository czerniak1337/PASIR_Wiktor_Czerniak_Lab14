import { useEffect } from "react";
import { toast } from "react-toastify";
import { useAuth } from "../../context/AuthContext";

interface GroupNotification {
  type: "GROUP_EXPENSE_ADDED";
  groupId: number | string;
  groupName: string;
  title: string;
  amount: number;
  userShare: number;
  createdByEmail: string;
  message: string;
}

const getWebSocketUrl = (token: string) => {
  const protocol =
    globalThis.location.protocol === "https:"
      ? "wss"
      : "ws";

  const encodedToken =
    encodeURIComponent(token);

  return `${protocol}://localhost:8080/ws/group-notifications?token=${encodedToken}`;
};

const GroupNotificationsListener = () => {
  const { isAuthenticated } = useAuth();

  useEffect(() => {
    if (!isAuthenticated) {
      return;
    }

    const token =
      globalThis.localStorage.getItem(
        "accessToken"
      );

    if (!token) {
      return;
    }

    const socketUrl =
      getWebSocketUrl(token);

    const socket =
      new WebSocket(socketUrl);

    socket.onmessage = (event) => {
      console.log(
        "RAW WS DATA:",
        event.data
      );

      try {
        const notification =
          JSON.parse(
            event.data
          ) as GroupNotification;

        console.log(
          "PARSED:",
          notification
        );

        if (
          notification.type ===
          "GROUP_EXPENSE_ADDED"
        ) {
          console.log(
            "SHOWING TOAST"
          );

          console.log(
            "TOAST MESSAGE:",
            notification.message
          );

          toast.info(
            notification.message,
            {
              autoClose: false,
            }
          );
        }
      } catch (error) {
        console.error(
          "Nie udało się obsłużyć komunikatu grupowego:",
          error
        );
      }
    };

    socket.onerror = (error) => {
      console.error(
        "Błąd połączenia WebSocket z komunikatami grupowymi:",
        error
      );
    };

    return () => {
      socket.close();
    };
  }, [isAuthenticated]);

  return null;
};

export default GroupNotificationsListener;