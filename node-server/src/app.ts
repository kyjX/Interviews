import express, { type Express } from "express";
import { healthRouter } from "./routes/health.js";

export function createApp(): Express {
  const app = express();

  app.use(express.json());
  app.use("/api/health", healthRouter);

  app.get("/api/version", (_req, res) => {
    res.json({ name: "written-test-node-server", version: "1.0.0" });
  });

  app.use((_req, res) => {
    res.status(404).json({ error: "Not Found" });
  });

  return app;
}
