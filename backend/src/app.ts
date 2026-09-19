import express, {
  NextFunction,
  type Express,
  type Request,
  type Response,
} from "express";
import { validationResult } from "express-validator";
import { AppRoutes } from "./routes/AppRoutes";

export function createApp(): Express {
  const app = express();

  app.use(express.json());

  AppRoutes.forEach((route) => {
    (app as any)[route.method](
      route.path,
      route.validation,
      async (req: Request, res: Response, next: NextFunction) => {
        const errors = validationResult(req);

        if (!errors.isEmpty()) {
          return res.status(400).send({ errors: errors.array() });
        }

        try {
          await route.action(req, res, next);
        } catch (error) {
          console.error(error);
          return res.status(500).json({ error: "Internal Server Error" });
        }
      },
    );
  });

  app.use((_req, res) => {
    res.status(404).json({ error: "Not Found" });
  });

  return app;
}
