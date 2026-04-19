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
let currentBot = 0;
let totalConnected = 0;
let totalQuests = 0;
let totalCancelled = 0;

const host = 'localhost';
const port = 25565;

console.log('='.repeat(55));
console.log('     ПОСЛЕДОВАТЕЛЬНЫЙ ЗАПУСК БОТОВ');
console.log(`     Ботов: ${botsCount}`);
console.log('='.repeat(55));
console.log('');

function runBot() {
    if (currentBot >= botsCount) {
        console.log('\n' + '='.repeat(55));
        console.log('           РЕЗУЛЬТАТЫ');
        console.log('='.repeat(55));
        console.log(`  ✅ Запущено ботов: ${totalConnected}`);
        console.log(`  📝 Выполнено /quest: ${totalQuests}`);
        console.log(`  ❌ Отменено квестов: ${totalCancelled}`);
        console.log('='.repeat(55));
        return;
    }
    
    const botName = generateRandomName();
    currentBot++;
    
    console.log(`\n🤖 [${botName}] (${currentBot}/${botsCount})`);
    
    const client = mineflayer.createBot({
        host: host,
        port: port,
        username: botName,
        version: '1.21.11',
        auth: 'offline'
    });
    
    let questDone = false;
    let cancelDone = false;
    
    client.on('login', () => {
        totalConnected++;
        console.log(`  ✅ Подключился`);
        
        setTimeout(() => {
            console.log(`  📝 -> /quest`);
            client.chat('/quest');
            totalQuests++;
        }, 2000);
        
        setTimeout(() => {
            console.log(`  ❌ -> /quest cancel`);
            client.chat('/quest cancel');
            totalCancelled++;
        }, 5000);
        
        setTimeout(() => {
            client.end();
        }, 7000);
    });
    
    client.on('message', (message) => {
        const msg = message.toString();
        if (msg.includes('НОВЫЙ КВЕСТ') && !questDone) {
            questDone = true;
            console.log(`  💬 ✅ Получил квест`);
        }
        if (msg.includes('КВЕСТ ОТМЕНЁН') && !cancelDone) {
            cancelDone = true;
            console.log(`  💬 ❌ Отменил квест`);
        }
    });
    
    client.on('error', (err) => {
        console.log(`  ❌ Ошибка: ${err.message}`);
    });
    
    client.on('end', () => {
        console.log(`  👋 Отключился`);
        setTimeout(() => runBot(), 3000);
    });
}

runBot();