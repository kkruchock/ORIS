window.onload = function () {
    const inputTask = document.querySelector('input[name11="task"]')
    const addButton = document.querySelector('.task-adder button')
    const taskList = document.querySelector('.task-list')

//сделаем Enter = addButton
    inputTask.addEventListener('keydown', function (event) {
        if (event.key === 'Enter') {
            addButton.click(); //симулируем клик
        }
    });

//добавление задачи
    addButton.addEventListener('click', function() {
        console.log('the add button was pressed');
        const taskText = inputTask.value.trim() //распаковываем Input + обрезаем пробелы
        if (taskText !== '') {
            const taskItem = document.createElement('li') //создали элемент в списке
            taskItem.textContent = taskText; //добавили текст
            const deleteButton = document.createElement('button') //создали кнопку
            deleteButton.textContent = 'done'; //текст для кнопки
            deleteButton.classList.add('delete-button'); //чтобы оформить в css
            taskItem.appendChild(deleteButton) //добавили подкласс в li
            taskList.appendChild(taskItem); //добавили в общий список
            inputTask.value = ''; //очищаем
            inputTask.focus(); //курсор сразу на поле ввода
        }
    });

//удаление задачи
    taskList.addEventListener('click', function(event) {
        console.log('the delete button was pressed');
        if (event.target.classList.contains('delete-button')) { //если класс кнопки ...
            event.target.closest('li').remove(); //поднимаемся от кнопки вверх к li и удаляем
        }
    });
}

// const inputTask = document.querySelector('input[name="task"]')
// const addButton = document.querySelector('.task-adder button')
// const taskList = document.querySelector('.task-list')
//
// //сделаем Enter = addButton
// inputTask.addEventListener('keydown', function (event) {
//     if (event.key === 'Enter') {
//         addButton.click(); //симулируем клик
//     }
// });
//
// //добавление задачи
// addButton.addEventListener('click', function() {
//     console.log('the add button was pressed');
//     const taskText = inputTask.value.trim() //распаковываем Input + обрезаем пробелы
//     if (taskText !== '') {
//         const taskItem = document.createElement('li') //создали элемент в списке
//         taskItem.textContent = taskText; //добавили текст
//         const deleteButton = document.createElement('button') //создали кнопку
//         deleteButton.textContent = 'done'; //текст для кнопки
//         deleteButton.classList.add('delete-button'); //чтобы оформить в css
//         taskItem.appendChild(deleteButton) //добавили подкласс в li
//         taskList.appendChild(taskItem); //добавили в общий список
//         inputTask.value = ''; //очищаем
//         inputTask.focus(); //курсор сразу на поле ввода
//     }
// });
//
// //удаление задачи
// taskList.addEventListener('click', function(event) {
//     console.log('the delete button was pressed');
//     if (event.target.classList.contains('delete-button')) { //если класс кнопки ...
//         event.target.closest('li').remove(); //поднимаемся от кнопки вверх к li и удаляем
//     }
// });