import React from 'react'
import WordCloud from 'react-d3-cloud'
import 'whatwg-fetch'

const fontSizeMapper = word => Math.log2(word.value) * word.value * 0.3
const rotate = word => 0

export default class App extends React.Component {

  constructor (props) {
    super(props)
    this.state = {
      data: []
    }
  }

  fetchData = (path, event) => {
    return fetch ('http://localhost:9000' + path, {
      method: 'POST',
      mode: 'cors',
      credentials: 'include',
      headers: {'Content-Type': 'application/json; charset=UTF-8'},
      body: JSON.stringify({event: event})
    })
    .then(response => response.json())
    .then(json => this.setState({data: json}))
    .catch(ex => this.setState({data: []}))
  }

  render() {
      return (
          <div>
            <button type="button" onClick={this.fetchData.bind(this, '/', 'cloud')} >送信</button>

            <WordCloud
              data={this.state.data}
              fontSizeMapper={fontSizeMapper}
              rotate={rotate}
              width={1200}
              height={620}
            />
          </div>
          )
      }
  }