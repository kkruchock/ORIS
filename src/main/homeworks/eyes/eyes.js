const pupils = document.querySelectorAll('.pupil');

document.addEventListener('mousemove', (event) => {

    pupils.forEach(pupil => {

        const rect = pupil.parentElement.getBoundingClientRect();

        //так глаза будут двигаться по квадрату
        // pupil.style.left = Math.max(0, Math.min(event.pageX - rect.left, rect.width - pupil.offsetWidth)) + 'px';
        // pupil.style.top = Math.max(0, Math.min(event.pageY - rect.top, rect.height - pupil.offsetHeight)) + 'px';

        //центр глаза (сокета)
        const centerX = rect.left + rect.width / 2;
        const centerY = rect.top + rect.height / 2;

        //катеты
        let deltaX = event.pageX - centerX;
        let deltaY = event.pageY - centerY;

        const maxDistance = rect.width / 2 - pupil.offsetWidth / 2; //ограничиваем радиусом
        const distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY); //гипотенуза

        if (distance > maxDistance) {
            //делим катеты на гипотенузы - нормализуем
            deltaX = (deltaX / distance) * maxDistance; //по сути косинус умножаем да радиус и получаем катет нормальный длины, где гипотенуза = радиусу
            deltaY = (deltaY / distance) * maxDistance;
        }

        //считаем не относительно (0,0), а относительно центра
        pupil.style.left = (rect.width / 2 + deltaX - pupil.offsetWidth / 2) + 'px';
        pupil.style.top = (rect.height / 2 + deltaY - pupil.offsetHeight / 2) + 'px';
    });
});