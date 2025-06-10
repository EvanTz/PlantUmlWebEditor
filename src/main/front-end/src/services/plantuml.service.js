import axios from 'axios';

const API_URL = '/api/plantuml';

const renderDiagram = async (source, format = 'SVG') => {
    try {
        const response = await axios.post(
            `${API_URL}/render?format=${format}`,
            source,
            {
                headers: {
                    'Content-Type': 'text/plain',
                },
            }
        );
        return response.data;

    } catch (error) {
        console.error('Error rendering diagram:', error);
        throw error;
    }
};


const generateImage = async (source, format = 'PNG') => {
    try {
        const response = await axios.post(
            `${API_URL}/image?format=${format}`,
            source,
            {
                headers: {
                    'Content-Type': 'text/plain',
                },
                responseType: 'blob', // Needed for binary image data
            }
        );
        return response.data;

    } catch (error) {
        console.error('Error generating image:', error);
        throw error;
    }

};

const plantUmlService = {
    renderDiagram,
    generateImage,
};

export default plantUmlService;
