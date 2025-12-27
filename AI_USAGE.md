# AI Usage

Briefly describe how you used AI (tools, prompts, code snippets, fixes). Be concise and honest (using AI is fine; it's just a tool; we want to know your method). If you did not use AI, state that here.

## Assumptions

1. **Queue with all ineligible members**: When a reservation queue exists but everyone in it is at their borrow limit, anyone can borrow the book directly.

2. **Members at limit stay in queue**: When ineligible members are skipped during handoff, they remain in the queue and become eligible again when they return books.

3. **Automatic assignment after returns**: When a member returns a book and becomes eligible, the system automatically checks if they can receive any books they've reserved.

4. **Members at limit can still reserve**: Members who have reached their borrow limit can still add reservations. They're queued and will receive the book when they become eligible.

5. **Cannot reserve already borrowed books**: Members cannot reserve a book they currently have on loan.
