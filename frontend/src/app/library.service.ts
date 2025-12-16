export interface Book {
  id: string;
  title: string;
  loanedTo: string | null;
  reservationQueue: string[];
}

export interface Member {
  id: string;
  name: string;
}

export interface ActionResult {
  ok: boolean;
  reason?: string;
  nextMemberId?: string | null;
}

export class LibraryApiService {
  constructor(private readonly baseUrl = 'http://localhost:8080/api') {}

  async books(): Promise<Book[]> {
    const res = await fetch(`${this.baseUrl}/books`);
    const data = await res.json();
    return data.items as Book[];
  }

  async members(): Promise<Member[]> {
    const res = await fetch(`${this.baseUrl}/members`);
    const data = await res.json();
    return data.items as Member[];
  }

  async borrow(bookId: string, memberId: string): Promise<ActionResult> {
    return this.post('/borrow', { bookId, memberId });
  }

  async reserve(bookId: string, memberId: string): Promise<ActionResult> {
    return this.post('/reserve', { bookId, memberId });
  }

  async cancelReservation(bookId: string, memberId: string): Promise<ActionResult> {
    return this.post('/cancel-reservation', { bookId, memberId });
  }

  async returnBook(bookId: string): Promise<ActionResult> {
    return this.post('/return', { bookId });
  }

  async createBook(id: string, title: string): Promise<ActionResult> {
    return this.post('/books', { id, title });
  }

  async updateBook(id: string, title: string): Promise<ActionResult> {
    return this.put('/books', { id, title });
  }

  async deleteBook(id: string): Promise<ActionResult> {
    return this.delete('/books', { id });
  }

  async createMember(id: string, name: string): Promise<ActionResult> {
    return this.post('/members', { id, name });
  }

  async updateMember(id: string, name: string): Promise<ActionResult> {
    return this.put('/members', { id, name });
  }

  async deleteMember(id: string): Promise<ActionResult> {
    return this.delete('/members', { id });
  }

  private async post(path: string, payload: Record<string, string>): Promise<ActionResult> {
    const res = await fetch(`${this.baseUrl}${path}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });
    return res.json();
  }

  private async put(path: string, payload: Record<string, string>): Promise<ActionResult> {
    const res = await fetch(`${this.baseUrl}${path}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });
    return res.json();
  }

  private async delete(path: string, payload: Record<string, string>): Promise<ActionResult> {
    const res = await fetch(`${this.baseUrl}${path}`, {
      method: 'DELETE',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });
    return res.json();
  }
}

