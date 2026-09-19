import type { NextFunction, Request, Response } from "express";
import { OAuth2Client } from "google-auth-library";

const googleClient = new OAuth2Client();

export class AppController {
  async health(
    _req: Request,
    res: Response,
    next: NextFunction,
  ): Promise<void> {
    res.json({ status: "ok" });
  }

  async loginWithGoogle(req: Request, res: Response, next: NextFunction) {
    const { token } = req.body;

    if (!token) {
      return res.status(400).json({
        error: "Missing Google ID token",
      });
    }

    if (!process.env.GOOGLE_WEB_CLIENT_ID) {
      return res.status(500).json({
        error: "Missing Google client ID",
      });
    }

    try {
      const ticket = await googleClient.verifyIdToken({
        idToken: token,
        audience: process.env.GOOGLE_WEB_CLIENT_ID,
        // Specify the WEB_CLIENT_ID of the app that accesses the backend
        // Or, if multiple clients access the backend:
        //[WEB_CLIENT_ID_1, WEB_CLIENT_ID_2, WEB_CLIENT_ID_3]
      });
      const payload = ticket.getPayload();
      // This ID is unique to each Google Account, making it suitable for use as a primary key
      // during account lookup. Email is not a good choice because it can be changed by the user.

      if (!payload) {
        return res.status(401).json({
          error: "Invalid Google ID token",
        });
      }

      return res.json({
        authenticated: true,
        name: payload.name,
        email: payload.email,
        googleUserId: payload.sub,
      });
    } catch (error) {
      console.error("Google token verification failed:", error);

      return res.status(401).json({
        authenticated: false,
        error: "Invalid Google ID token",
      });
    }
  }

  async getServerIp(_req: Request, res: Response, next: NextFunction) {
    res.json({
      ip: "placeholder",
    });
  }

  async getServerTime(_req: Request, res: Response, next: NextFunction) {
    res.json({
      time: new Date().toISOString(),
    });
  }

  async getStudentName(_req: Request, res: Response, next: NextFunction) {
    res.json({
      firstName: process.env.STUDENT_NAME || "YourFirstName",
      lastName: process.env.STUDENT_LAST_NAME || "YourLastName",
    });
  }
}
