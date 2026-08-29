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
// 3. Mutation: Finish the task and save the generated deck ID
export const finishTask = mutation({
  args: {
    taskId: v.string(),
    deckId: v.string(),
  },
  handler: async (ctx, args) => {
    const existing = await ctx.db
      .query("tasks")
      .withIndex("by_taskId", (q) => q.eq("taskId", args.taskId))
      .unique();

    if (existing) {
      await ctx.db.patch(existing._id, {
        taskStatus: "Complete",
        generatedDeckId: args.deckId,
        devinLogs: [...existing.devinLogs, "Generation pipeline finished successfully!"],
      });
    }
  },
});

// 4. Mutation: Save generated deck details
export const saveGeneratedDeck = mutation({
  args: {
    deckId: v.string(),
    name: v.string(),
    description: v.string(),
    category: v.string(),
  },
  handler: async (ctx, args) => {
    const existing = await ctx.db
      .query("decks")
      .withIndex("by_langerId", (q) => q.eq("id", args.deckId))
      .unique();

    if (existing) {
      await ctx.db.patch(existing._id, {
        name: args.name,
        description: args.description,
        category: args.category,
      });
    } else {
      await ctx.db.insert("decks", {
        id: args.deckId,
        name: args.name,
        description: args.description,
        category: args.category,
        dailyLimit: 20,
      });
    }
  },
});

// 5. Mutation: Save generated card details
export const saveGeneratedCard = mutation({
  args: {
    deckId: v.string(),
    word: v.string(),
    phonetic: v.string(),
    meaning: v.string(),
    example: v.string(),
    imageUrl: v.string(),
    audioUrl: v.optional(v.string()),
  },
  handler: async (ctx, args) => {
    const cardId = `${Date.now()}-${Math.random().toString(36).substring(2, 9)}`;
    await ctx.db.insert("flashcards", {
      id: cardId,
      deckId: args.deckId,
      word: args.word,
      phonetic: args.phonetic,
      meaning: args.meaning,
      example: args.example,
      imageUrl: args.imageUrl,
      audioUrl: args.audioUrl,
    });
  },
});

// 6. Query: Get generated deck details
export const getGeneratedDeck = query({
  args: { deckId: v.string() },
  handler: async (ctx, args) => {
    return await ctx.db
      .query("decks")
      .withIndex("by_langerId", (q) => q.eq("id", args.deckId))
      .unique();
  },
});

// 7. Query: Get generated cards list for a deck
export const getGeneratedCards = query({
  args: { deckId: v.string() },
  handler: async (ctx, args) => {
    return await ctx.db
      .query("flashcards")
      .withIndex("by_deckId", (q) => q.eq("deckId", args.deckId))
      .collect();
  },
});
