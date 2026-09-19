import { AppController } from "../controllers/AppController";
import { body, param } from "express-validator";

const controller = new AppController();

export const AppRoutes = [
  {
    method: "get",
    path: "/health",
    action: controller.health,
    validation: [],
  },
  {
    method: "post",
    path: "/auth/google",
    action: controller.loginWithGoogle,
    validation: [], // to add later
  },
  {
    method: "get",
    path: "/server/ip",
    action: controller.getServerIp,
    validation: [],
  },
  {
    method: "get",
    path: "/server/time",
    action: controller.getServerTime,
    validation: [],
  },
];
