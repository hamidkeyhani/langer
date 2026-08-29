import { query, mutation } from "./_generated/server";
import { v } from "convex/values";

// 1. Subscription Query: Gets the current state of a task
export const getTaskState = query({
  args: { taskId: v.string() },
  handler: async (ctx, args) => {
    const task = await ctx.db
      .query("tasks")
      .withIndex("by_taskId", (q) => q.eq("taskId", args.taskId))
      .unique();
      
    // Return a default state if the task record doesn't exist yet
    return task ?? {
      taskId: args.taskId,
      taskStatus: "Initialized (Waiting for backend)",
      extractedMarkdown: "",
      devinSessionId: "",
      devinLogs: [],
    };
  },
});

// 2. Mutation: Updates the task state (called by Android UI or Devin)
export const updateState = mutation({
  args: {
    taskId: v.string(),
    status: v.string(),
    markdown: v.union(v.string(), v.null()),
    sessionId: v.union(v.string(), v.null()),
  },
  handler: async (ctx, args) => {
    const existing = await ctx.db
      .query("tasks")
      .withIndex("by_taskId", (q) => q.eq("taskId", args.taskId))
      .unique();

    const markdownVal = args.markdown === null ? undefined : args.markdown;
    const sessionVal = args.sessionId === null ? undefined : args.sessionId;

    if (existing) {
      // Update existing task record
      await ctx.db.patch(existing._id, {
        taskStatus: args.status,
        extractedMarkdown: markdownVal,
        devinSessionId: sessionVal,
        // Append a log entry
        devinLogs: [...existing.devinLogs, `Status changed to: ${args.status}`],
      });
    } else {
      // Create new task record
      await ctx.db.insert("tasks", {
        taskId: args.taskId,
        taskStatus: args.status,
        extractedMarkdown: markdownVal,
        devinSessionId: sessionVal,
        devinLogs: [`Created task: ${args.status}`],
      });
    }
  },
});
