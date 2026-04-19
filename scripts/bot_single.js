const mineflayer = require('mineflayer');

// Генерация случайного имени
const names = [
    "Avetisyan", "Vasilev", "Dzyankovskij", "Kalugin", "Karpov",
    "Kachenkov", "Kirin", "Magaziner", "Moiseeva", "Novikov",
    "Palekhov", "Pelevina", "Sargsyan", "Sakharov", "Urasov", "Shirokshin"
];

function generateRandomName() {
    const lastName = names[Math.floor(Math.random() * names.length)];
    const number = Math.floor(Math.random() * 900) + 100;
    return `${lastName}${number}`;
}

const botName = generateRandomName();
const host = 'localhost';
const port = 25565;

console.log('='.repeat(55));
console.log('     ЗАПУСК БОТА');
console.log(`     Имя: ${botName}`);
console.log('='.repeat(55));
console.log('');

const client = mineflayer.createBot({
    host: host,
    port: port,
    username: botName,
    version: '1.21.11',
    auth: 'offline'
});

client.on('login', () => {
    console.log(`✅ [${botName}] Подключился к серверу`);
    
    // Через 2 секунды выполняем /quest
    setTimeout(() => {
        console.log(`  📝 [${botName}] -> /quest`);
        client.chat('/quest');
    }, 2000);
    
    // Через 5 секунд отменяем квест
    setTimeout(() => {
        console.log(`  ❌ [${botName}] -> /quest cancel`);
        client.chat('/quest cancel');
    }, 5000);
    
    // Через 7 секунд отключаемся
    setTimeout(() => {
        console.log(`  👋 [${botName}] Отключение`);
        client.end();
    }, 7000);
});

client.on('message', (message) => {
    const msg = message.toString();
    if (msg.includes('НОВЫЙ КВЕСТ')) {
        console.log(`  💬 [${botName}] ✅ Получил квест`);
    }
    if (msg.includes('КВЕСТ ОТМЕНЁН')) {
        console.log(`  💬 [${botName}] ❌ Квест отменён`);
    }
});

client.on('error', (err) => {
    console.log(`❌ Ошибка: ${err.message}`);
});

client.on('end', () => {
    console.log(`  ✅ [${botName}] Сессия завершена`);
});