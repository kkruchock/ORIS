const square = document.querySelector('.square')
const container = document.querySelector('.container')

const containerWidth = container.clientWidth; //без учета границ
const containerHeight = container.clientHeight;

const squareWidth = square.offsetWidth; //с учетом границ
const squareHeight = square.offsetHeight;

let speedX = -5
let speedY = -5

let squareX = 0
let squareY = 0

setInterval(moveSquare => {

    if (squareX + squareWidth >= containerWidth) {
        speedX *= -1
    } else if (squareX <= 0) {
        speedX *= -1
    }

    if (squareY + squareHeight >= containerHeight) {
        speedY *= -1
    } else if (squareY <= 0) {
        speedY *= -1
    }

    squareX += speedX
    squareY += speedY

    square.style.left = squareX + 'px'
    square.style.top = squareY + 'px'
},16)