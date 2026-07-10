const initSqlJs = require('C:\\Users\\86181\\AppData\\Local\\Temp\\node_modules\\sql.js');

async function main() {
    const SQL = await initSqlJs();
    const fs = require('fs');
    const buffer = fs.readFileSync('C:\\Users\\86181\\.local\\share\\opencode\\opencode.db');
    const db = new SQL.Database(buffer);
    
    const targetDir = process.argv[2] || 'MIS_Design';
    
    const rows = db.exec(`
        SELECT id, title, agent, directory, time_created, 
               tokens_input, tokens_output
        FROM session 
        WHERE directory LIKE '%${targetDir}%'
        ORDER BY time_created DESC
    `);
    
    if (rows.length === 0) {
        console.log('没有找到包含 "' + targetDir + '" 的对话记录');
        db.close();
        return;
    }

    console.log('=== "' + targetDir + '" 相关的对话记录 ===\n');
    let count = 0;
    for (const row of rows[0].values) {
        count++;
        console.log('[' + count + '] ' + (row[1] || '(无标题)'));
        console.log('    ID: ' + row[0]);
        console.log('');
    }
    console.log('共 ' + count + ' 条');
    
    console.log('\n=== 使用方式 ===');
    console.log('查看全部:    node 查看历史对话.js');
    console.log('按项目查看: node 查看历史对话.js MIS_Design');
    console.log('导出某条:   opencode export <ID>');
    
    db.close();
}

main().catch(console.error);