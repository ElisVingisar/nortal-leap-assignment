# AI Usage

I used **Claude Code** throughout this assignment as a learning resource and development partner.

**Understanding the codebase:**
Since I was unfamiliar with hexagonal architecture, I asked Claude to explain the repository pattern used in the project:
> "Explain me the logic and connection with domain (Member.java) -> port (MemberRepository.java) -> adapter (MemberRepositoryAdapter.java) -> jpa (JpaMemberRepository.java). I haven't worked with this structure before, so I need to understand the logic behind it and if some changes are made in service or other java classes, then how they should or should not be changed?"

I also used it to clarify Spring Boot concepts I was less familiar with.

**Implementation:**
- I discussed my logic and ideas with Claude to validate my approach made sense
- I asked for code solution ideas for the reservation queue automation methods (like `tryHandoffBook` and `processReservationsForMember`) - this was the most complex part where I needed the most assistance
- I asked how certain methods could be less expensive (Claude suggested using `countByLoanedTo` and `findAvailableBooksWithMemberInQueue` queries)

**Debugging:**
When tests failed (e.g., getting `BOOK_UNAVAILABLE` instead of `QUEUE_EXISTS`), I shared the failing scenarios with Claude to help identify the issues.

## Assumptions

1. **Queue with all ineligible members**: When a reservation queue exists but everyone in it is at their borrow limit, anyone can borrow the book directly.

2. **Members at limit stay in queue**: When ineligible members are skipped during handoff, they remain in the queue and become eligible again when they return books.

3. **Automatic assignment after returns**: When a member returns a book and becomes eligible, the system automatically checks if they can receive any books they've reserved.

4. **Members at limit can still reserve**: Members who have reached their borrow limit can still add reservations. They're queued and will receive the book when they become eligible.

5. **Cannot reserve already borrowed books**: Members cannot reserve a book they currently have on loan.
