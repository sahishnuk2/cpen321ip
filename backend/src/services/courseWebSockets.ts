import { WebSocket, WebSocketServer } from "ws";

const courseSocket = new WebSocket("wss://8.229.22.124");

const frontendSocketServer = new WebSocketServer({
  port: 3001,
});

const frontendClients = new Set<WebSocket>();

frontendSocketServer.on("connection", (frontendSocket) => {
  console.log("Android app connected");

  frontendClients.add(frontendSocket);

  frontendSocket.on("close", () => {
    console.log("Android app disconnected");
    frontendClients.delete(frontendSocket);
  });

  frontendSocket.on("error", () => {
    frontendClients.delete(frontendSocket);
  });
});

courseSocket.on("open", () => {
  console.log("Connected to course WebSocket");
});

courseSocket.on("message", (data) => {
  console.log("Received pixel:", data.toString());

  // Get from course and send to frontend
  for (const frontendSocket of frontendClients) {
    if (frontendSocket.readyState === WebSocket.OPEN) {
      frontendSocket.send(data.toString());
    }
  }
});

courseSocket.on("error", (error) => {
  console.error("Course WebSocket error:", error);
});

courseSocket.on("close", () => {
  console.log("Course WebSocket closed");
});
