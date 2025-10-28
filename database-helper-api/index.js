const express = require('express');
const mysql = require('mysql2/promise');
const app = express();
app.use(express.json());

// --- UPDATED CONFIGURATION ---
// host is now the name of the MySQL service defined in docker-compose.yml
const dbConfig = {
    host: 'mysql_db',
    user: 'root',
    password: 'John@123', // Must match the value in docker-compose.yml
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
        // The fix is here: Use an empty array [] as the default for params.
        const [rows] = await dbPool.execute(query, params || []);
        res.status(200).json(rows);
    } catch (error) {
        console.error('Database query failed:', error);
        res.status(500).json({ error: 'Database query failed', details: error.message });
    }
});

const PORT = 3001;
app.listen(PORT, () => {
    console.log(`DB Helper API running on http://localhost:${PORT}`);
});
