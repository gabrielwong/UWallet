package ca.uwallet.main.model;


/**
 * Data class representing a transaction.
 * @author Gabriel, Seikun
 */

public class Transaction {

	private int id;
	private int amount;
	private long date;
	private int transactionType;
	private int terminal;
	
	private static final int DEFAULT_ID = 0;
	private static final int DEFAULT_AMOUNT = 0;
	private static final long DEFAULT_DATE = 0;
	private static final int DEFAULT_TRANSACTION_TYPE = 0;
	private static final int DEFAULT_TERMINAL = 0;


	public Transaction() {
		this(DEFAULT_ID, DEFAULT_AMOUNT, DEFAULT_DATE, DEFAULT_TRANSACTION_TYPE, DEFAULT_TERMINAL);
	}

	/**
	 * Constructs a transaction object.
	 * @param id The id in the SQL table.
	 * @param amount The transaction amount in cents.
	 * @param date The date the transaction occured in Unix time.
	 * @param transactionType The transaction type (flex or meal plan, etc).
	 * @param terminal The transaction terminal (vendor / description).
	 */
	public Transaction(int id, int amount, long date, int transactionType, int terminal) {
		this.id = id;
		this.amount = amount;
		this.date = date;
		this.transactionType = transactionType;
		this.terminal = terminal;
	}
	
	public Transaction(int amount, long date, int transactionType, int terminal) {
		this(DEFAULT_ID, amount, date, transactionType, terminal);
	}

	public int getID() {
		return this.id;
	}

	public void setID(int id) {
		this.id = id;
	}

	public int getAmount() {
		return this.amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

	public int getType() {
		return transactionType;
	}

	public void setTransactionType(int transactionType) {
		this.transactionType = transactionType;
	}

	public int getTerminal() {
		return terminal;
	}

	public void setTerminal(int terminal) {
		this.terminal = terminal;
	}
}