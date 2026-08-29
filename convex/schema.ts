import { defineSchema, defineTable } from "convex/server";
import { v } from "convex/values";

export default defineSchema({
  // This will store our task state sync
  tasks: defineTable({
    taskId: v.string(),
    taskStatus: v.string(),
    extractedMarkdown: v.optional(v.string()),
    devinSessionId: v.optional(v.string()),
    devinLogs: v.array(v.string()),
  }).index("by_taskId", ["taskId"]),
});
