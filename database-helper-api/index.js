const express = require('express');
const mysql = require('mysql2/promise');
const app = express();
app.use(express.json());

// --- IMPORTANT ---
// Node.js running on the host must connect to the Docker container via localhost
// and the mapped external port (3308).
const dbConfig = {
    host: 'localhost',
    port: 3308, // The port mapped in docker-compose.yml
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
        // Use an empty array [] as the default for params for prepared statements
        const [rows] = await dbPool.execute(query, params || []);
        res.status(200).json(rows);
    } catch (error) {
        console.error('Database query failed:', error);
        // Return error details to the client for debugging
        res.status(500).json({ error: 'Database query failed', details: error.message });
    }
});

const PORT = 3001;
app.listen(PORT, () => {
    console.log(`DB Helper API running on http://localhost:${PORT}`);
});
