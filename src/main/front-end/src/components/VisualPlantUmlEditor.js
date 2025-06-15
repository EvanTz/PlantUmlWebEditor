import React, { useState, useEffect } from 'react';
import {
    Box,
    Paper,
    Typography,
    Button,
    Dialog,
    DialogTitle,
    DialogContent,
    DialogActions,
    TextField,
    Select,
    MenuItem,
    FormControl,
    InputLabel,
    IconButton,
    Grid,
    Card,
    CardContent,
    Chip,
    Divider,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import ArrowForwardIcon from '@mui/icons-material/ArrowForward';
import RemoveIcon from '@mui/icons-material/Remove';


const VisualPlantUmlEditor = ({ content, onChange }) => {
    // Main state for diagram elements
    const [classes, setClasses] = useState([]);
    const [relationships, setRelationships] = useState([]);

    // Dialog state management
    const [openDialog, setOpenDialog] = useState(false);
    const [dialogType, setDialogType] = useState('');
    const [editingClassId, setEditingClassId] = useState(null);
    const [editingRelationshipId, setEditingRelationshipId] = useState(null);

    // Form states for class editing
    const [className, setClassName] = useState('');
    const [classType, setClassType] = useState('class');
    const [stereotype, setStereotype] = useState('');
    const [attributes, setAttributes] = useState([]);
    const [methods, setMethods] = useState([]);

    // Form states for relationship editing
    const [relationshipFrom, setRelationshipFrom] = useState('');
    const [relationshipTo, setRelationshipTo] = useState('');
    const [relationshipType, setRelationshipType] = useState('association');
    const [relationshipLabel, setRelationshipLabel] = useState('');
    const [sourceMultiplicity, setSourceMultiplicity] = useState('');
    const [targetMultiplicity, setTargetMultiplicity] = useState('');

    // Custom multiplicity range state
    const [sourceCustomRange, setSourceCustomRange] = useState({ min: '', max: '' });
    const [targetCustomRange, setTargetCustomRange] = useState({ min: '', max: '' });
    const [sourceIsCustomRange, setSourceIsCustomRange] = useState(false);
    const [targetIsCustomRange, setTargetIsCustomRange] = useState(false);


    // Class types for dropdown
    const classTypes = [
        { value: 'class', label: 'Class' },
        { value: 'abstract', label: 'Abstract Class' },
        { value: 'interface', label: 'Interface' },
        { value: 'enum', label: 'Enumeration' }
    ];

    // Relationship types for dropdown
    const relationshipTypes = [
        { value: 'association', label: 'Association (-->)', syntax: '-->' },
        { value: 'inheritance', label: 'Inheritance (<|--)', syntax: '<|--' },
        { value: 'implementation', label: 'Implementation (<|..)', syntax: '<|..' },
        { value: 'composition', label: 'Composition (*--)', syntax: '*--' },
        { value: 'aggregation', label: 'Aggregation (o--)', syntax: 'o--' },
        { value: 'dependency', label: 'Dependency (..>)', syntax: '..>' }
    ];


    // Multiplicity options  for UML relationships
    const multiplicityOptions = [
        { value: '', label: 'None' },
        { value: '1', label: '1 (exactly one)' },
        { value: '0..1', label: '0..1 (zero or one)' },
        { value: '1..*', label: '1..* (one or more)' },
        { value: '0..*', label: '0..* (zero or more)' },
        { value: '*', label: '* (many)' },
        { value: '2', label: '2 (exactly two)' },
        { value: '2..*', label: '2..* (two or more)' },
        { value: 'custom', label: 'Custom Range (specify numbers)' }
    ];

    // Visibility options
    const visibilityOptions = [
        { value: 'public', label: 'Public (+)', symbol: '+' },
        { value: 'private', label: 'Private (-)', symbol: '-' },
        { value: 'protected', label: 'Protected (#)', symbol: '#' },
        { value: 'package', label: 'Package (~)', symbol: '~' }
    ];

    // Parse existing PlantUML content when it changes
    useEffect(() => {
        if (content) {
            parseContent(content);
        }
    }, [content]);


    // Generate PlantUML immediately when classes or relationships change
    useEffect(() => {
        if (classes.length > 0 || relationships.length > 0) {
            generatePlantUML();
        }
    }, [classes, relationships]);


    //Core function that converts visual representation to PlantUML
    const generatePlantUML = () => {
        let uml = '@startuml\n';

        // Add classes
        classes.forEach(cls => {
            const classKeyword = cls.type === 'abstract' ? 'abstract class' : cls.type;
            uml += `${classKeyword} ${cls.name}`;

            if (cls.stereotype) {
                uml += ` <<${cls.stereotype}>>`;
            }

            // Add braces only when the class is not  empty
            if (cls.attributes.length > 0 || cls.methods.length > 0) {
                uml += ' {\n';

                // Add attributes
                cls.attributes.forEach(attr => {
                    if (attr.name.trim()) {
                        const visibility = getVisibilitySymbol(attr.visibility);
                        const type = attr.type ? ` : ${attr.type}` : '';
                        uml += `    ${visibility}${attr.name}${type}\n`;
                    }
                });

                // Add separator if both exist
                if (cls.attributes.length > 0 && cls.methods.length > 0) {
                    uml += '    --\n';
                }

                // Add methods
                cls.methods.forEach(method => {
                    if (method.name.trim()) {
                        const visibility = getVisibilitySymbol(method.visibility);
                        const returnType = method.returnType && method.returnType !== 'void' ? ` : ${method.returnType}` : '';
                        const params = method.parameters || '';
                        uml += `    ${visibility}${method.name}(${params})${returnType}\n`;
                    }
                });

                uml += '}\n';
            }
            uml += '\n'; // Extra spacing between classes
        });

        // Add relationships
        relationships.forEach(rel => {
            const syntax = getRelationshipSyntax(rel.type);
            let relationshipLine = `${rel.from}`;

            // Get actual source multiplicity
            const actualSourceMultiplicity = rel.sourceMultiplicity;
            if (actualSourceMultiplicity) {
                relationshipLine += ` "${actualSourceMultiplicity}"`;
            }

            relationshipLine += ` ${syntax}`;

            // Get actual target multiplicity
            const actualTargetMultiplicity = rel.targetMultiplicity;
            if (actualTargetMultiplicity) {
                relationshipLine += ` "${actualTargetMultiplicity}"`;
            }

            relationshipLine += ` ${rel.to}`;

            // Add label if specified
            if (rel.label) {
                relationshipLine += ` : ${rel.label}`;
            }

            uml += relationshipLine + '\n';
        });

        uml += '@enduml';

        onChange(uml); // Send update back to ProjectEditor component
    };

    // Parse PlantUML content and update visual state
    // Could be improved/expanded
    const parseContent = (umlContent) => {
        const lines = umlContent.split('\n');
        const parsedClasses = [];
        const parsedRelationships = [];
        let currentClass = null;
        let insideClass = false;

        lines.forEach(line => {
            const trimmedLine = line.trim();

            // Skip empty lines and comments
            if (!trimmedLine || trimmedLine.startsWith("'")) return;

            // Start/end markers
            if (trimmedLine.startsWith('@startuml') || trimmedLine.startsWith('@enduml')) {
                return;
            }

            // Parse class declarations
            const classMatch = trimmedLine.match(/^(class|abstract class|interface|enum)\s+([^\s{]+)(?:\s*<<([^>]+)>>)?\s*(\{)?/);
            if (classMatch) {
                const type = classMatch[1] === 'abstract class' ? 'abstract' : classMatch[1];
                currentClass = {
                    id: Date.now() + Math.random(),
                    name: classMatch[2],
                    type: type,
                    stereotype: classMatch[3] || '',
                    attributes: [],
                    methods: []
                };
                parsedClasses.push(currentClass);
                insideClass = !!classMatch[4]; // Check if opening brace is present
                return;
            }

            // Handle opening brace on separate line
            if (trimmedLine === '{' && currentClass) {
                insideClass = true;
                return;
            }

            // Handle closing brace
            if (trimmedLine === '}' && insideClass) {
                insideClass = false;
                currentClass = null;
                return;
            }


            // Parse class members (attributes and methods)
            if (insideClass && currentClass) {
                // Skip separator lines
                if (trimmedLine === '--' || trimmedLine === '__') {
                    return;
                }

                // Parse attributes and methods
                // Regex: [visibility][name][(params)][ : type]
                const memberMatch = trimmedLine.match(/^\s*([+\-#~]?)([^:(]+)(\([^)]*\))?(?:\s*:\s*(.+))?/);
                if (memberMatch) {
                    const visibility = getVisibilityFromSymbol(memberMatch[1] || '+');
                    const name = memberMatch[2].trim();
                    const hasParams = !!memberMatch[3];
                    const type = memberMatch[4] ? memberMatch[4].trim() : '';

                    if (hasParams) {
                        // It is a method
                        const parameters = memberMatch[3] ? memberMatch[3].slice(1, -1) : ''; // Remove parentheses
                        currentClass.methods.push({
                            name: name,
                            visibility: visibility,
                            returnType: type || 'void',
                            parameters: parameters
                        });
                    } else {
                        // It is an attribute
                        currentClass.attributes.push({
                            name: name,
                            visibility: visibility,
                            type: type
                        });
                    }
                }
                return;
            }

            // Parse relationships
            const relationshipMatch = trimmedLine.match(/([^\s"]+)(?:\s+"([^"]*)")?\s*([<|.\-*o>]+)(?:\s+"([^"]*)")?\s*([^\s"]+)(?:\s*:\s*(.+))?/);
            if (relationshipMatch && !insideClass) {
                const relationshipType = getRelationshipTypeFromSyntax(relationshipMatch[3]);
                parsedRelationships.push({
                    id: Date.now() + Math.random(),
                    from: relationshipMatch[1],
                    sourceMultiplicity: relationshipMatch[2] || '',
                    to: relationshipMatch[5],
                    targetMultiplicity: relationshipMatch[4] || '',
                    type: relationshipType,
                    label: relationshipMatch[6] ? relationshipMatch[6].trim() : ''
                });
            }
        });


        // console.log('Parsed classes:', parsedClasses);
        // console.log('Parsed relationships:', parsedRelationships);

        // Update state with parsed
        setClasses(parsedClasses);
        setRelationships(parsedRelationships);
    };

    // Helper function
    const getVisibilityFromSymbol = (symbol) => {
        const symbols = {
            '+': 'public',
            '-': 'private',
            '#': 'protected',
            '~': 'package'
        };
        return symbols[symbol] || 'public';
    };

    // Helper function to get actual multiplicity value (including custom ranges)
    const getActualMultiplicity = (multiplicityValue, customRange, isCustom) => {
        if (isCustom && customRange.min !== '' && customRange.max !== '') {
            return `${customRange.min}..${customRange.max}`;
        } else if (isCustom && customRange.min !== '' && customRange.max === '') {
            return `${customRange.min}..*`;
        } else if (isCustom && customRange.min === '' && customRange.max !== '') {
            return `0..${customRange.max}`;
        }
        return multiplicityValue;
    };

    // Helper function to parse multiplicity back to form state
    const parseMultiplicity = (value) => {
        if (!value) return { isCustom: false, value: '', customRange: { min: '', max: '' } };

        // Check if it's a custom range (contains .. with numbers)
        const customMatch = value.match(/^(\d+)\.\.(\d+|\*)$/);

        if (customMatch) {
            const min = customMatch[1];
            const max = customMatch[2] === '*' ? '' : customMatch[2];

            // Check if this is a predefined option
            const predefined = multiplicityOptions.find(opt => opt.value === value);

            if (predefined) {
                return { isCustom: false, value: value, customRange: { min: '', max: '' } };
            } else {
                return { isCustom: true, value: 'custom', customRange: { min, max } };
            }
        }

        return { isCustom: false, value: value, customRange: { min: '', max: '' } };
    };

    // Map relationships
    const getRelationshipTypeFromSyntax = (syntax) => {
        const typeMap = {
            '-->': 'association',
            '<--': 'association',
            '<|--': 'inheritance',
            '--|>': 'inheritance',
            '<|..': 'implementation',
            '..|>': 'implementation',
            '*--': 'composition',
            '--*': 'composition',
            'o--': 'aggregation',
            '--o': 'aggregation',
            '..>': 'dependency',
            '<..': 'dependency'
        };
        return typeMap[syntax] || 'association';
    };

    const getVisibilitySymbol = (visibility) => {
        const symbols = {
            'public': '+',
            'private': '-',
            'protected': '#',
            'package': '~'
        };
        return symbols[visibility] || '+';
    };

    const getRelationshipSyntax = (type) => {
        const syntaxMap = {
            'association': '-->',
            'inheritance': '<|--',
            'implementation': '<|..',
            'composition': '*--',
            'aggregation': 'o--',
            'dependency': '..>'
        };
        return syntaxMap[type] || '-->';
    };

    // Reset all fields to empty
    const resetForm = () => {
        setClassName('');
        setClassType('class');
        setStereotype('');
        setAttributes([]);
        setMethods([]);
        setRelationshipFrom('');
        setRelationshipTo('');
        setRelationshipType('association');
        setRelationshipLabel('');
        setSourceMultiplicity('');
        setTargetMultiplicity('');
        setSourceCustomRange({ min: '', max: '' });
        setTargetCustomRange({ min: '', max: '' });
        setSourceIsCustomRange(false);
        setTargetIsCustomRange(false);
        setEditingClassId(null);
        setEditingRelationshipId(null);
    };


    // Dialog for adding-editing classes adn relationships
    const handleOpenDialog = (type, item = null) => {
        setDialogType(type);
        setOpenDialog(true);

        if (item && type === 'class') {

            setEditingClassId(item.id);
            setClassName(item.name);
            setClassType(item.type);
            setStereotype(item.stereotype || '');
            setAttributes([...item.attributes]); // Create new array to avoid mutation
            setMethods([...item.methods]); // Create new array to avoid mutation

            // console.log('Editing class:', item);
            // console.log('Set attributes:', [...item.attributes]);

        } else if (item && type === 'relationship') {

            setEditingRelationshipId(item.id);
            setRelationshipFrom(item.from);
            setRelationshipTo(item.to);
            setRelationshipType(item.type);
            setRelationshipLabel(item.label || '');

            // Parse source multiplicity
            const sourceParsed = parseMultiplicity(item.sourceMultiplicity || '');
            setSourceMultiplicity(sourceParsed.value);
            setSourceCustomRange(sourceParsed.customRange);
            setSourceIsCustomRange(sourceParsed.isCustom);

            // Parse target multiplicity
            const targetParsed = parseMultiplicity(item.targetMultiplicity || '');
            setTargetMultiplicity(targetParsed.value);
            setTargetCustomRange(targetParsed.customRange);
            setTargetIsCustomRange(targetParsed.isCustom);

        } else {
            resetForm();
        }

    };

    const handleCloseDialog = () => {
        setOpenDialog(false);
        resetForm();
    };

    const handleSaveClass = () => {
        const classData = {
            id: editingClassId || Date.now(),
            name: className,
            type: classType,
            stereotype: stereotype,
            attributes: [...attributes], // Ensure we have a clean copy
            methods: [...methods] // Ensure we have a clean copy
        };

        // console.log('Saving class  data:', classData);

        if (editingClassId) {
            // Update existing class
            setClasses(prev => prev.map(cls =>
                cls.id === editingClassId ? classData : cls
            ));

        } else {
            // Add new class
            setClasses(prev => [...prev, classData]);
        }

        handleCloseDialog();
    };


    const handleSaveRelationship = () => {
        // Get actual multiplicity values (including custom ranges)
        const actualSourceMultiplicity = getActualMultiplicity(sourceMultiplicity, sourceCustomRange, sourceIsCustomRange);
        const actualTargetMultiplicity = getActualMultiplicity(targetMultiplicity, targetCustomRange, targetIsCustomRange);

        const relationshipData = {
            id: editingRelationshipId || Date.now(),
            from: relationshipFrom,
            to: relationshipTo,
            type: relationshipType,
            label: relationshipLabel,
            sourceMultiplicity: actualSourceMultiplicity,
            targetMultiplicity: actualTargetMultiplicity
        };

        // console.log('Saving relationship with multiplicities:', {
        //     source: actualSourceMultiplicity,
        //     target: actualTargetMultiplicity
        // });

        if (editingRelationshipId) {
            setRelationships(prev => prev.map(rel =>
                rel.id === editingRelationshipId ? relationshipData : rel
            ));
        } else {
            setRelationships(prev => [...prev, relationshipData]);
        }

        handleCloseDialog();
    };

    const handleDeleteClass = (id) => {
        setClasses(prev => prev.filter(cls => cls.id !== id));
    };

    const handleDeleteRelationship = (id) => {
        setRelationships(prev => prev.filter(rel => rel.id !== id));
    };

    //
    const handleSourceMultiplicityChange = (value) => {
        setSourceMultiplicity(value);
        if (value === 'custom') {
            setSourceIsCustomRange(true);
        } else {
            setSourceIsCustomRange(false);
            setSourceCustomRange({ min: '', max: '' });
        }
    };

    const handleTargetMultiplicityChange = (value) => {
        setTargetMultiplicity(value);
        if (value === 'custom') {
            setTargetIsCustomRange(true);
        } else {
            setTargetIsCustomRange(false);
            setTargetCustomRange({ min: '', max: '' });
        }
    };


    const addAttribute = () => {
        const newAttribute = {
            name: '',
            type: '',
            visibility: 'public'
        };
        setAttributes(prev => [...prev, newAttribute]);
        // console.log('Added attribute, total:', attributes.length + 1);
    };

    const updateAttribute = (index, field, value) => {
        setAttributes(prev => {
            const newAttributes = [...prev];
            newAttributes[index] = { ...newAttributes[index], [field]: value };
            // console.log('Updated attribute', index, field, value, 'New attributes:', newAttributes);
            return newAttributes;
        });
    };

    const removeAttribute = (index) => {
        setAttributes(prev => prev.filter((_, i) => i !== index));
    };


    const addMethod = () => {
        const newMethod = {
            name: '',
            returnType: 'void',
            visibility: 'public',
            parameters: ''

        };
        setMethods(prev => [...prev, newMethod]);
    };

    const updateMethod = (index, field, value) => {
        setMethods(prev => {
            const newMethods = [...prev];
            newMethods[index] = { ...newMethods[index], [field]: value };
            return newMethods;
        });
    };

    const removeMethod = (index) => {
        setMethods(prev => prev.filter((_, i) => i !== index));
    };

    return (
        <Box>
            {/* Debug Info */}
            {/*<Paper sx={{ p: 1, mb: 2, backgroundColor: '#f0f0f0' }}>*/}
            {/*    <Typography variant="caption" fontWeight="bold">Debug: </Typography>*/}
            {/*    <Typography variant="caption">*/}
            {/*        Classes: {classes.length} |*/}
            {/*        Dialog Open: {openDialog ? 'Yes' : 'No'} |*/}
            {/*        Dialog Type: {dialogType} |*/}
            {/*        Form Attributes: {attributes.length}*/}
            {/*    </Typography>*/}
            {/*    {dialogType === 'relationship' && openDialog && (*/}
            {/*        <Typography variant="caption" display="block">*/}
            {/*            Source Multiplicity: {sourceIsCustomRange ? `Custom(${sourceCustomRange.min}..${sourceCustomRange.max || '*'})` : sourceMultiplicity} |*/}
            {/*            Target Multiplicity: {targetIsCustomRange ? `Custom(${targetCustomRange.min}..${targetCustomRange.max || '*'})` : targetMultiplicity}*/}
            {/*        </Typography>*/}
            {/*    )}*/}
            {/*    {classes.map((cls, idx) => (*/}
            {/*        <Typography key={idx} variant="caption" display="block">*/}
            {/*            • {cls.name}: {cls.attributes.length} attrs, {cls.methods.length} methods*/}
            {/*            {cls.attributes.length > 0 && ` [${cls.attributes.map(a => a.name || 'empty').join(', ')}]`}*/}
            {/*        </Typography>*/}
            {/*    ))}*/}
            {/*</Paper>*/}

            {/* Two-column layout for classes and relationships */}
            <Box sx={{ mb: 2 }}>
                <Grid container spacing={2}>

                    {/* Left Column - Classes */}
                    <Grid item xs={12} md={6}>
                        <Paper sx={{ p: 2, height: 'fit-content' }}>
                            {/* Classes header with action button */}
                            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                                <Typography variant="h6">Classes ({classes.length})</Typography>
                                <Button
                                    variant="contained"
                                    size="small"
                                    startIcon={<AddIcon />}
                                    onClick={() => handleOpenDialog('class')}
                                >
                                    Add Class
                                </Button>
                            </Box>

                            {/* Classes list */}
                            {classes.length > 0 ? (
                                <Grid container spacing={2}>
                                    {classes.map((cls) => (
                                        <Grid item xs={12} key={cls.id}>
                                            <Card>
                                                <CardContent sx={{ background: '#d2f5cf' }}> {/* Light green background for classes */}
                                                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start', mb: 1 }}>
                                                        <Box>
                                                            <Typography variant="h6">
                                                                {cls.type !== 'class' && (
                                                                    <Chip label={cls.type} size="small" sx={{ mr: 1 }} />
                                                                )}
                                                                {cls.name}
                                                            </Typography>
                                                            {cls.stereotype && (
                                                                <Typography variant="caption" color="text.secondary">
                                                                    &lt;&lt;{cls.stereotype}&gt;&gt;
                                                                </Typography>
                                                            )}
                                                        </Box>
                                                        <Box>
                                                            <IconButton size="small" onClick={() => handleOpenDialog('class', cls)}>
                                                                <EditIcon />
                                                            </IconButton>
                                                            <IconButton size="small" onClick={() => handleDeleteClass(cls.id)}>
                                                                <DeleteIcon />
                                                            </IconButton>
                                                        </Box>
                                                    </Box>

                                                    <Typography variant="body2" color="text.secondary">
                                                        {cls.attributes.length} attributes, {cls.methods.length} methods
                                                    </Typography>

                                                    {/* Show preview of attributes if any exist */}
                                                    {cls.attributes.length > 0 && (
                                                        <Box sx={{ mt: 1 }}>
                                                            <Typography variant="caption" fontWeight="bold">Attributes:</Typography>
                                                            {cls.attributes.slice(0, 3).map((attr, idx) => (
                                                                <Typography key={idx} variant="caption" display="block" sx={{ ml: 1 }}>
                                                                    {getVisibilitySymbol(attr.visibility)}{attr.name}
                                                                    {attr.type && ` : ${attr.type}`}
                                                                </Typography>
                                                            ))}
                                                            {cls.attributes.length > 3 && (
                                                                <Typography variant="caption" color="text.secondary">
                                                                    ... and {cls.attributes.length - 3} more
                                                                </Typography>
                                                            )}
                                                        </Box>
                                                    )}
                                                </CardContent>
                                            </Card>
                                        </Grid>
                                    ))}
                                </Grid>
                            ) : (
                                <Box sx={{
                                    textAlign: 'center',
                                    py: 4,
                                    color: 'text.secondary',
                                    // border: '2px dashed #ccc',
                                    // borderRadius: 1
                                }}>
                                    <Typography variant="body2">
                                        No classes yet. Click "Add Class" to create your first class.
                                    </Typography>
                                </Box>
                            )}
                        </Paper>
                    </Grid>

                    {/* Right Column - Relationships */}
                    <Grid item xs={12} md={6}>
                        <Paper sx={{ p: 2, height: 'fit-content' }}>
                            {/* Relationships header with action button */}
                            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                                <Typography variant="h6">Relationships ({relationships.length})</Typography>
                                <Button
                                    variant="contained"
                                    size="small"
                                    startIcon={<AddIcon />}
                                    onClick={() => handleOpenDialog('relationship')}
                                    disabled={classes.length < 2} // Need at least 2 classes to create a relationship
                                >
                                    Add Relationship
                                </Button>
                            </Box>

                            {/* Relationships list */}
                            {relationships.length > 0 ? (
                                <Grid container spacing={2}>
                                    {relationships.map((rel) => (
                                        <Grid item xs={12} key={rel.id}>
                                            <Card>
                                                <CardContent sx={{ background: '#c3e8f5' }}> {/* Light blue background for relationships */}
                                                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                                        <Box sx={{ display: 'flex', alignItems: 'center' }}>
                                                            <ArrowForwardIcon sx={{ mr: 1 }} />
                                                            <Box>
                                                                <Typography variant="body2">
                                                                    {rel.sourceMultiplicity && `"${rel.sourceMultiplicity}" `}
                                                                    {rel.from} {getRelationshipSyntax(rel.type)} {rel.to}
                                                                    {rel.targetMultiplicity && ` "${rel.targetMultiplicity}"`}
                                                                </Typography>
                                                                {rel.label && (
                                                                    <Typography variant="caption" color="text.secondary">
                                                                        Label: {rel.label}
                                                                    </Typography>
                                                                )}
                                                                <Typography variant="caption" display="block" color="text.secondary">
                                                                    {relationshipTypes.find(t => t.value === rel.type)?.label || rel.type}
                                                                </Typography>
                                                            </Box>
                                                        </Box>
                                                        <Box>
                                                            <IconButton size="small" onClick={() => handleOpenDialog('relationship', rel)}>
                                                                <EditIcon />
                                                            </IconButton>
                                                            <IconButton size="small" onClick={() => handleDeleteRelationship(rel.id)}>
                                                                <DeleteIcon />
                                                            </IconButton>
                                                        </Box>
                                                    </Box>
                                                </CardContent>
                                            </Card>
                                        </Grid>
                                    ))}
                                </Grid>
                            ) : (
                                <Box sx={{
                                    textAlign: 'center',
                                    py: 4,
                                    color: 'text.secondary',
                                    // border: '2px dashed #ccc',
                                    // borderRadius: 1
                                }}>
                                    <Typography variant="body2">
                                        {classes.length < 2
                                            ? "Create at least 2 classes to add relationships between them."
                                            : "No relationships yet. Click \"Add Relationship\" to connect your classes."
                                        }
                                    </Typography>
                                </Box>
                            )}
                        </Paper>
                    </Grid>
                </Grid>
            </Box>

            {/* Dialog for adding/editing classes and relationships */}
            <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="md" fullWidth>
                <DialogTitle>
                    {(editingClassId || editingRelationshipId) ? 'Edit' : 'Add'} {' '}
                    {dialogType === 'class' ? 'Class' : 'Relationship'}
                </DialogTitle>
                <DialogContent>
                    {/* Class editing form */}
                    {dialogType === 'class' && (
                        <Box>
                            {/* Basic class information */}
                            <Grid container spacing={2} sx={{ mb: 2 }}>
                                <Grid item xs={12} sm={6}>
                                    <TextField
                                        label="Class Name"
                                        fullWidth
                                        value={className}
                                        onChange={(e) => setClassName(e.target.value)}
                                    />
                                </Grid>
                                <Grid item xs={12} sm={6}>
                                    <FormControl fullWidth>
                                        <InputLabel>Type</InputLabel>
                                        <Select
                                            value={classType}
                                            label="Type"
                                            onChange={(e) => setClassType(e.target.value)}
                                        >
                                            {classTypes.map(type => (
                                                <MenuItem key={type.value} value={type.value}>
                                                    {type.label}
                                                </MenuItem>
                                            ))}
                                        </Select>
                                    </FormControl>
                                </Grid>
                                <Grid item xs={12}>
                                    <TextField
                                        label="Stereotype (optional)"
                                        fullWidth
                                        value={stereotype}
                                        onChange={(e) => setStereotype(e.target.value)}
                                    />
                                </Grid>
                            </Grid>

                            <Divider sx={{ my: 2 }} />

                            {/* Attributes section */}
                            <Box sx={{ mb: 3 }}>
                                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
                                    <Typography variant="h6">Attributes ({attributes.length})</Typography>
                                    <Button size="small" onClick={addAttribute} startIcon={<AddIcon />}>
                                        Add Attribute
                                    </Button>
                                </Box>
                                {attributes.map((attr, index) => (
                                    <Box key={index} sx={{ display: 'flex', gap: 1, mb: 1, alignItems: 'center' }}>
                                        <FormControl size="small" sx={{ minWidth: 120 }}>
                                            <InputLabel>Visibility</InputLabel>
                                            <Select
                                                value={attr.visibility}
                                                label="Visibility"
                                                onChange={(e) => updateAttribute(index, 'visibility', e.target.value)}
                                            >
                                                {visibilityOptions.map(vis => (
                                                    <MenuItem key={vis.value} value={vis.value}>
                                                        {vis.label}
                                                    </MenuItem>
                                                ))}
                                            </Select>
                                        </FormControl>
                                        <TextField
                                            size="small"
                                            label="Name"
                                            value={attr.name}
                                            onChange={(e) => updateAttribute(index, 'name', e.target.value)}
                                            sx={{ flexGrow: 1 }}
                                        />
                                        <TextField
                                            size="small"
                                            label="Type"
                                            value={attr.type}
                                            onChange={(e) => updateAttribute(index, 'type', e.target.value)}
                                            sx={{ minWidth: 100 }}
                                        />
                                        <IconButton size="small" onClick={() => removeAttribute(index)} color="error">
                                            <RemoveIcon />
                                        </IconButton>
                                    </Box>
                                ))}
                            </Box>

                            <Divider sx={{ my: 2 }} />

                            {/* Methods section */}
                            <Box>
                                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
                                    <Typography variant="h6">Methods ({methods.length})</Typography>
                                    <Button size="small" onClick={addMethod} startIcon={<AddIcon />}>
                                        Add Method
                                    </Button>
                                </Box>
                                {methods.map((method, index) => (
                                    <Box key={index} sx={{ mb: 2, p: 1, border: '1px solid #ddd', borderRadius: 1 }}>
                                        <Box sx={{ display: 'flex', gap: 1, mb: 1, alignItems: 'center' }}>
                                            <FormControl size="small" sx={{ minWidth: 120 }}>
                                                <InputLabel>Visibility</InputLabel>
                                                <Select
                                                    value={method.visibility}
                                                    label="Visibility"
                                                    onChange={(e) => updateMethod(index, 'visibility', e.target.value)}
                                                >
                                                    {visibilityOptions.map(vis => (
                                                        <MenuItem key={vis.value} value={vis.value}>
                                                            {vis.label}
                                                        </MenuItem>
                                                    ))}
                                                </Select>
                                            </FormControl>
                                            <TextField
                                                size="small"
                                                label="Method Name"
                                                value={method.name}
                                                onChange={(e) => updateMethod(index, 'name', e.target.value)}
                                                sx={{ flexGrow: 1 }}
                                            />
                                            <TextField
                                                size="small"
                                                label="Return Type"
                                                value={method.returnType}
                                                onChange={(e) => updateMethod(index, 'returnType', e.target.value)}
                                                sx={{ minWidth: 100 }}
                                            />
                                            <IconButton size="small" onClick={() => removeMethod(index)} color="error">
                                                <RemoveIcon />
                                            </IconButton>
                                        </Box>
                                        <TextField
                                            size="small"
                                            label="Parameters"
                                            value={method.parameters}
                                            onChange={(e) => updateMethod(index, 'parameters', e.target.value)}
                                            fullWidth
                                        />
                                    </Box>
                                ))}
                            </Box>
                        </Box>
                    )}

                    {/* Relationship editing form */}
                    {dialogType === 'relationship' && (
                        <Box>
                            <Grid container spacing={2}>
                                <Grid item xs={12} sm={6}>
                                    <FormControl fullWidth>
                                        <InputLabel>From</InputLabel>
                                        <Select
                                            value={relationshipFrom}
                                            label="From"
                                            onChange={(e) => setRelationshipFrom(e.target.value)}
                                        >
                                            {classes.map(cls => (
                                                <MenuItem key={cls.id} value={cls.name}>
                                                    {cls.name}
                                                </MenuItem>
                                            ))}
                                        </Select>
                                    </FormControl>
                                </Grid>
                                <Grid item xs={12} sm={6}>
                                    <FormControl fullWidth>
                                        <InputLabel>To</InputLabel>
                                        <Select
                                            value={relationshipTo}
                                            label="To"
                                            onChange={(e) => setRelationshipTo(e.target.value)}
                                        >
                                            {classes.map(cls => (
                                                <MenuItem key={cls.id} value={cls.name}>
                                                    {cls.name}
                                                </MenuItem>
                                            ))}
                                        </Select>
                                    </FormControl>
                                </Grid>
                                <Grid item xs={12}>
                                    <FormControl fullWidth>
                                        <InputLabel>Relationship Type</InputLabel>
                                        <Select
                                            value={relationshipType}
                                            label="Relationship Type"
                                            onChange={(e) => setRelationshipType(e.target.value)}
                                        >
                                            {relationshipTypes.map(rel => (
                                                <MenuItem key={rel.value} value={rel.value}>
                                                    {rel.label}
                                                </MenuItem>
                                            ))}
                                        </Select>
                                    </FormControl>
                                </Grid>

                                {/* Source multiplicity */}
                                <Grid item xs={12} sm={6}>
                                    <FormControl fullWidth>
                                        <InputLabel>Source Multiplicity</InputLabel>
                                        <Select
                                            value={sourceMultiplicity}
                                            label="Source Multiplicity"
                                            onChange={(e) => handleSourceMultiplicityChange(e.target.value)}
                                        >
                                            {multiplicityOptions.map(mult => (
                                                <MenuItem key={mult.value} value={mult.value}>
                                                    {mult.label}
                                                </MenuItem>
                                            ))}
                                        </Select>
                                    </FormControl>

                                    {/* Custom range input for source multiplicity */}
                                    {sourceIsCustomRange && (
                                        <Box sx={{ mt: 1, display: 'flex', gap: 1, alignItems: 'center' }}>
                                            <TextField
                                                size="small"
                                                label="Min"
                                                type="number"
                                                value={sourceCustomRange.min}
                                                onChange={(e) => setSourceCustomRange(prev => ({ ...prev, min: e.target.value }))}
                                                inputProps={{ min: 0 }}
                                                sx={{ width: 80 }}
                                            />
                                            <Typography variant="body2">..</Typography>
                                            <TextField
                                                size="small"
                                                label="Max"
                                                type="number"
                                                value={sourceCustomRange.max}
                                                onChange={(e) => setSourceCustomRange(prev => ({ ...prev, max: e.target.value }))}
                                                inputProps={{ min: 0 }}
                                                placeholder="* for unlimited"
                                                sx={{ width: 120 }}
                                            />
                                            <Typography variant="caption" color="text.secondary">
                                                Leave max empty for unlimited (*)
                                            </Typography>
                                        </Box>
                                    )}
                                </Grid>

                                {/* Target multiplicity */}
                                <Grid item xs={12} sm={6}>
                                    <FormControl fullWidth>
                                        <InputLabel>Target Multiplicity</InputLabel>
                                        <Select
                                            value={targetMultiplicity}
                                            label="Target Multiplicity"
                                            onChange={(e) => handleTargetMultiplicityChange(e.target.value)}
                                        >
                                            {multiplicityOptions.map(mult => (
                                                <MenuItem key={mult.value} value={mult.value}>
                                                    {mult.label}
                                                </MenuItem>
                                            ))}
                                        </Select>
                                    </FormControl>
                                    {/* Custom range input for target multiplicity */}
                                    {targetIsCustomRange && (
                                        <Box sx={{ mt: 1, display: 'flex', gap: 1, alignItems: 'center' }}>
                                            <TextField
                                                size="small"
                                                label="Min"
                                                type="number"
                                                value={targetCustomRange.min}
                                                onChange={(e) => setTargetCustomRange(prev => ({ ...prev, min: e.target.value }))}
                                                inputProps={{ min: 0 }}
                                                sx={{ width: 80 }}
                                            />
                                            <Typography variant="body2">..</Typography>
                                            <TextField
                                                size="small"
                                                label="Max"
                                                type="number"
                                                value={targetCustomRange.max}
                                                onChange={(e) => setTargetCustomRange(prev => ({ ...prev, max: e.target.value }))}
                                                inputProps={{ min: 0 }}
                                                placeholder="* for unlimited"
                                                sx={{ width: 120 }}
                                            />
                                            <Typography variant="caption" color="text.secondary">
                                                Leave max empty for unlimited (*)
                                            </Typography>
                                        </Box>
                                    )}
                                </Grid>
                                <Grid item xs={12}>
                                    <TextField
                                        label="Label (optional)"
                                        fullWidth
                                        value={relationshipLabel}
                                        onChange={(e) => setRelationshipLabel(e.target.value)}
                                    />
                                </Grid>
                            </Grid>
                        </Box>
                    )}
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleCloseDialog}>Cancel</Button>
                    <Button
                        onClick={dialogType === 'class' ? handleSaveClass : handleSaveRelationship}
                        variant="contained"
                    >
                        {(editingClassId || editingRelationshipId) ? 'Update' : 'Add'}
                    </Button>
                </DialogActions>
            </Dialog>
        </Box>

    );


};


export default VisualPlantUmlEditor;