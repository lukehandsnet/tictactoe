from flask import Flask, render_template, jsonify, request
from flask_cors import CORS

app = Flask(__name__)
CORS(app)

class GameLogic:
    def __init__(self):
        self.board = [[0] * 3 for _ in range(3)]  # 0: empty, 1: player 1, 2: player 2
        self.winning_line = []

    def check_win(self, player):
        # Check rows, columns, and diagonals
        for i in range(3):
            if all(self.board[i][j] == player for j in range(3)):
                self.winning_line = [(i, j) for j in range(3)]
                return True
            if all(self.board[j][i] == player for j in range(3)):
                self.winning_line = [(j, i) for j in range(3)]
                return True
        
        if all(self.board[i][i] == player for i in range(3)):
            self.winning_line = [(i, i) for i in range(3)]
            return True
        
        if all(self.board[i][2-i] == player for i in range(3)):
            self.winning_line = [(i, 2-i) for i in range(3)]
            return True
        
        return False

    def get_winning_line(self):
        return self.winning_line

    def is_draw(self):
        return all(self.board[i][j] != 0 for i in range(3) for j in range(3))

    def reset_game(self):
        self.board = [[0] * 3 for _ in range(3)]
        self.winning_line = []

    def make_move(self, row, col, player):
        if self.board[row][col] == 0:
            self.board[row][col] = player
            return True
        return False

game = GameLogic()

@app.route('/')
def index():
    return render_template('index.html')

@app.route('/move', methods=['POST'])
def make_move():
    data = request.get_json()
    row = data['row']
    col = data['col']
    player = data['player']

    if game.make_move(row, col, player):
        if game.check_win(player):
            return jsonify({
                'success': True,
                'status': 'win',
                'player': player,
                'winning_line': game.get_winning_line()
            })
        elif game.is_draw():
            return jsonify({
                'success': True,
                'status': 'draw'
            })
        else:
            return jsonify({
                'success': True,
                'status': 'continue'
            })
    return jsonify({'success': False})

@app.route('/reset', methods=['POST'])
def reset_game():
    game.reset_game()
    return jsonify({'success': True})

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=51621)