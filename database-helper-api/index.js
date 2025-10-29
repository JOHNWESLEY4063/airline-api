const express = require('express');
const mysql = require('mysql2/promise');
const app = express();
app.use(express.json());

// CORRECTED CONFIGURATION: Set host and port to connect to the local MySQL Workspace (port 3306).
// Using the specific host IP (10.4.5.160) for reliability, as confirmed by the successful Java tests.
const dbConfig = {
    host: '10.4.5.160', // MUST be the host IP address where your MySQL server is installed
    port: 3306,        // Standard MySQL port for your local workspace instance
    user: 'root',
    password: 'John@123',
    database: 'airline_db'
};

let pool;

async function getPool() {
    if (!pool) {
        pool = mysql.createPool(dbConfig);
    }
    return pool;
}

// This is the single endpoint that will execute our SQL queries
app.post('/query', async (req, res) => {
    const { query, params } = req.body;
    if (!query) {
        return res.status(400).json({ error: 'Query is required' });
    }

    try {
        const dbPool = await getPool();
        const [rows] = await dbPool.execute(query, params || []);
        res.status(200).json(rows);
    } catch (error) {
        console.error('Database query failed:', error);
        // The previous "Table doesn't exist" error must be solved by running your schema script in MySQL Workbench.
        res.status(500).json({ error: 'Database query failed', details: error.message });
    }
});

const PORT = 3001;
app.listen(PORT, () => {
    console.log(`DB Helper API running on http://localhost:${PORT}`);
});