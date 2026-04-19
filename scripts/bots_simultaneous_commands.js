const mineflayer = require('mineflayer');

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

const botsCount = 5;
const bots = [];
const clients = [];
let connected = 0;

const host = 'localhost';
const port = 25565;

console.log('='.repeat(55));
console.log('     ЗАПУСК БОТОВ');
console.log(`     Ботов: ${botsCount}`);
console.log('='.repeat(55));
console.log('');

for (let i = 0; i < botsCount; i++) {
    bots.push({ name: generateRandomName(), ready: false });
}

function sendCommandToAll(command) {
    console.log(`\nВСЕ БОТЫ ВЫПОЛНЯЮТ: ${command}\n`);
    clients.forEach(client => {
        if (client && client._client && client._client.state === 'play') {
            client.chat(command);
            console.log(`  [${client.username}] -> ${command}`);
        }
    });
}

bots.forEach((bot, index) => {
    setTimeout(() => {
        console.log(`[${bot.name}] Подключение...`);
        
        const client = mineflayer.createBot({
            host: host,
            port: port,
            username: bot.name,
            version: '1.21.11',
            auth: 'offline'
        });
        
        client.on('login', () => {
            connected++;
            console.log(`[${bot.name}] Подключился (${connected}/${botsCount})`);
            bot.ready = true;
            
            if (connected === botsCount) {
                console.log('\nВСЕ БОТЫ ПОДКЛЮЧИЛИСЬ');
                console.log('ОДНОВРЕМЕННОЕ ВЫПОЛНЕНИЕ КОМАНД\n');
                
                setTimeout(() => {
                    sendCommandToAll('/quest');
                }, 3000);
                
                setTimeout(() => {
                    sendCommandToAll('/quest cancel');
                }, 8000);
                
                setTimeout(() => {
                    console.log('\nОтключение...');
                    clients.forEach(c => c.end());
                }, 13000);
            }
        });
        
        client.on('message', (message) => {
            const msg = message.toString();
            if (msg.includes('НОВЫЙ КВЕСТ')) {
                console.log(`  [${client.username}] Получил квест`);
            }
            if (msg.includes('КВЕСТ ОТМЕНЁН')) {
                console.log(`  [${client.username}] Отменил квест`);
            }
        });
        
        client.on('error', (err) => {
            console.log(`[${bot.name}] Ошибка: ${err.message}`);
        });
        
        client.on('end', () => {
            console.log(`  [${bot.name}] Отключился`);
        });
        
        clients.push(client);
        
    }, index * 5000);
});

setTimeout(() => {
    console.log('\n' + '='.repeat(55));
    console.log('           РЕЗУЛЬТАТЫ');
    console.log('='.repeat(55));
    console.log(`  Подключилось: ${connected}/${botsCount}`);
    console.log('='.repeat(55));
}, 30000);